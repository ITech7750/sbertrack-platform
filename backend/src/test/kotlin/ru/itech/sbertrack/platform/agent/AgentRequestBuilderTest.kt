package ru.itech.sbertrack.platform.agent

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import ru.itech.sbertrack.platform.agent.application.service.AgentRequestBuilder
import ru.itech.sbertrack.platform.agent.domain.model.*
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.agent.dto.request.AgentMessageRequest
import ru.itech.sbertrack.platform.agent.infrastructure.adapter.AgentGatewayProperties
import ru.itech.sbertrack.platform.challengecase.domain.model.*
import ru.itech.sbertrack.platform.challengecase.domain.port.CaseDataPort
import ru.itech.sbertrack.platform.submission.domain.model.Submission
import ru.itech.sbertrack.platform.submission.domain.port.SubmissionDataPort
import ru.itech.sbertrack.platform.common.exception.BadRequestException
import ru.itech.sbertrack.platform.common.exception.ForbiddenException
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.common.model.Difficulty
import java.time.LocalDate
import java.util.UUID
import kotlin.test.*

class AgentRequestBuilderTest {
    private val agents = mock(AgentDataPort::class.java)
    private val cases = mock(CaseDataPort::class.java)
    private val submissions = mock(SubmissionDataPort::class.java)
    private val properties = AgentGatewayProperties()
    private val builder = AgentRequestBuilder(agents, cases, submissions, properties, jacksonObjectMapper())
    private val agent = AgentDefinition(code = "mentor", name = "Mentor", description = "Guide",
        specialization = AgentSpecialization.BACKEND_ARCHITECTURE, status = AgentStatus.ACTIVE,
        masterPromptId = UUID.randomUUID(), capabilities = emptyList())
    private val session = AgentSession(studentId = UUID.randomUUID(), caseId = null, agentId = agent.id)

    @Test fun `latest message once and old turns trimmed in complete pairs`() {
        properties.maxHistoryMessages = 4
        val history = listOf(AgentMessageRole.USER, AgentMessageRole.AGENT, AgentMessageRole.USER, AgentMessageRole.AGENT)
            .mapIndexed { i, role -> AgentMessage(sessionId = session.id, role = role, content = "turn$i") }
        val result = builder.build(agent, session.copy(messages = history), AgentMessageRequest("latest"))
        assertEquals(listOf("turn2", "turn3", "latest"), result.messages.map { it.content })
    }

    @Test fun `oversized old turn is dropped and latest preserved`() {
        properties.maxInputChars = 2000
        val old = AgentMessage(sessionId = session.id, role = AgentMessageRole.USER, content = "x".repeat(2500))
        val result = builder.build(agent, session.copy(messages = listOf(old)), AgentMessageRequest("latest"))
        assertEquals(listOf("latest"), result.messages.map { it.content })
    }

    @Test fun `oversized current request is rejected instead of truncating question`() {
        properties.maxInputChars = 2000
        assertFailsWith<BadRequestException> { builder.build(agent, session, AgentMessageRequest("x".repeat(2500))) }
        assertFailsWith<BadRequestException> { builder.build(agent, session, AgentMessageRequest("question", artifacts = List(21) { "url" })) }
    }

    @Test fun `only active prompt belonging to this agent is used`() {
        val prompt = MasterPrompt(id = agent.masterPromptId!!, agentId = agent.id, title = "Prompt", promptText = "Active rules",
            version = 1, status = MasterPromptStatus.ACTIVE, createdBy = UUID.randomUUID())
        `when`(agents.findMasterPromptById(prompt.id)).thenReturn(prompt)
        assertTrue(builder.build(agent, session, AgentMessageRequest("q")).systemPrompt.startsWith("Active rules"))
        `when`(agents.findMasterPromptById(prompt.id)).thenReturn(prompt.copy(status = MasterPromptStatus.ARCHIVED))
        assertFalse(builder.build(agent, session, AgentMessageRequest("q")).systemPrompt.contains("Active rules"))
        `when`(agents.findMasterPromptById(prompt.id)).thenReturn(prompt.copy(agentId = UUID.randomUUID()))
        assertFalse(builder.build(agent, session, AgentMessageRequest("q")).systemPrompt.contains("Active rules"))
    }

    @Test fun `case from server and only own saved work are sent as user data`() {
        val case = PracticalCase(trackId = UUID.randomUUID(), title = "Real case", shortDescription = "Short",
            fullDescription = "Server context", customerId = UUID.randomUUID(), customerName = "Customer",
            status = CaseStatus.PUBLISHED, difficulty = Difficulty.entries.first(), participantLimit = 10,
            expectedResult = "Result", feedbackMode = FeedbackMode.AGENT, competencyWeights = mapOf(Competency.AUTONOMY to 100),
            tags = emptyList(), deadline = LocalDate.now().plusDays(1))
        `when`(cases.findById(case.id)).thenReturn(case)
        `when`(submissions.findForAgentContext(session.studentId, case.id)).thenReturn(listOf(
            Submission(caseId = case.id, studentId = session.studentId, title = "My work", description = "My description", artifactUrl = "https://example.test/file")))
        val result = builder.build(agent, session.copy(caseId = case.id),
            AgentMessageRequest("Question", caseTitle = "Forged title", artifacts = listOf("draft reference")))
        val text = result.messages.single().content
        assertTrue(text.contains("Real case") && text.contains("My description") && text.contains("draft reference"))
        assertFalse(text.contains("Forged title")); assertFalse(result.systemPrompt.contains("My description"))
        verify(submissions).findForAgentContext(session.studentId, case.id)
        verify(submissions, never()).list()
        `when`(cases.findById(case.id)).thenReturn(case.copy(status = CaseStatus.DRAFT))
        assertFailsWith<ForbiddenException> { builder.build(agent, session.copy(caseId = case.id), AgentMessageRequest("q")) }
    }
}
