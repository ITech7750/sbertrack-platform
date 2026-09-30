package ru.itech.sbertrack.platform.agent

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import ru.itech.sbertrack.platform.agent.application.service.AgentRequestBuilder
import ru.itech.sbertrack.platform.agent.domain.model.AgentSession
import ru.itech.sbertrack.platform.agent.domain.model.AgentStatus
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.agent.dto.request.AgentMessageRequest
import ru.itech.sbertrack.platform.challengecase.domain.model.CaseStatus
import ru.itech.sbertrack.platform.challengecase.domain.port.CaseDataPort
import ru.itech.sbertrack.platform.submission.domain.model.Submission
import ru.itech.sbertrack.platform.submission.domain.model.SubmissionStatus
import ru.itech.sbertrack.platform.submission.domain.port.SubmissionDataPort
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort
import java.time.Instant
import java.util.UUID
import kotlin.test.*

@SpringBootTest
@Transactional
class AgentContextPersistenceTest {
    @Autowired lateinit var submissions: SubmissionDataPort
    @Autowired lateinit var users: UserDataPort
    @Autowired lateinit var cases: CaseDataPort
    @Autowired lateinit var agents: AgentDataPort
    @Autowired lateinit var builder: AgentRequestBuilder
    @Autowired lateinit var entityManager: EntityManager

    private fun student(): UUID {
        val id = UUID.randomUUID()
        return users.save(users.findByEmail("student@example.com")!!.copy(
            id = id, email = "context-$id@example.test",
        ), "test-password").id
    }

    private fun flushAndClear() { entityManager.flush(); entityManager.clear() }

    @Test fun `context includes newest work beyond five submissions and excludes other owners and cases`() {
        val studentId = student()
        val case = cases.list().first { it.status == CaseStatus.PUBLISHED }
        val otherCase = cases.list().first { it.id != case.id }
        val base = Instant.parse("2020-01-01T00:00:00Z")
        val works = (1..7).map { n ->
            submissions.save(Submission(id = UUID(0, n.toLong()), caseId = case.id, studentId = studentId,
                title = "work-$n", description = "draft-marker-$n", contentUpdatedAt = base.plusSeconds(n.toLong())))
        }
        submissions.save(Submission(caseId = case.id, studentId = student(), title = "other student", description = "private-owner-marker"))
        submissions.save(Submission(caseId = otherCase.id, studentId = studentId, title = "other case", description = "private-case-marker"))
        flushAndClear()

        assertEquals(works.takeLast(5).reversed().map { it.id }, submissions.findForAgentContext(studentId, case.id).map { it.id })
        val agent = agents.listAgents().first { it.status == AgentStatus.ACTIVE }
        val request = builder.build(agent, AgentSession(studentId = studentId, caseId = case.id, agentId = agent.id),
            AgentMessageRequest("Review my latest work"))
        val context = request.messages.last().content
        assertTrue(context.contains("draft-marker-7"))
        assertFalse(context.contains("draft-marker-1"))
        assertFalse(context.contains("private-owner-marker"))
        assertFalse(context.contains("private-case-marker"))
    }

    @Test fun `editing an old draft moves it into context but review metadata does not`() {
        val studentId = student()
        val caseId = cases.list().first().id
        val base = Instant.parse("2020-01-01T00:00:00Z")
        val works = (1..6).map { n -> submissions.save(Submission(
            caseId = caseId, studentId = studentId, title = "work-$n", description = "old",
            contentUpdatedAt = base.plusSeconds(n.toLong()),
        )) }
        flushAndClear()
        assertFalse(submissions.findForAgentContext(studentId, caseId).any { it.id == works.first().id })

        val oldest = submissions.findById(works.first().id)!!
        submissions.save(oldest.addFeedback(UUID.randomUUID(), SubmissionStatus.NEEDS_IMPROVEMENT))
        flushAndClear()
        assertEquals(oldest.contentUpdatedAt, submissions.findById(oldest.id)!!.contentUpdatedAt)
        assertFalse(submissions.findForAgentContext(studentId, caseId).any { it.id == oldest.id })

        val edited = submissions.findById(oldest.id)!!.updateDraft("edited", "new contents", null, null)
        submissions.save(edited)
        flushAndClear()
        val selected = submissions.findForAgentContext(studentId, caseId)
        assertEquals(5, selected.size)
        assertEquals(oldest.id, selected.first().id)
        assertEquals("new contents", selected.first().description)
        assertTrue(selected.first().contentUpdatedAt > oldest.contentUpdatedAt)
    }

    @Test fun `equal timestamps have a deterministic id tie breaker`() {
        val studentId = student()
        val caseId = cases.list().first().id
        val timestamp = Instant.parse("2020-01-01T00:00:00Z")
        (7 downTo 1).forEach { n -> submissions.save(Submission(
            id = UUID(0, n.toLong()), caseId = caseId, studentId = studentId,
            title = "work-$n", description = "same time", contentUpdatedAt = timestamp,
        )) }
        flushAndClear()
        assertEquals((1..5).map { UUID(0, it.toLong()) }, submissions.findForAgentContext(studentId, caseId).map { it.id })
    }
}
