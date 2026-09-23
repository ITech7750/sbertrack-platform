package ru.itech.sbertrack.platform.agent

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.mockito.Mockito.*
import ru.itech.sbertrack.platform.agent.domain.model.AgentGenerationRequest
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessage
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessageRole
import ru.itech.sbertrack.platform.agent.domain.model.AgentStatus
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.agent.domain.port.AgentGatewayPort
import ru.itech.sbertrack.platform.challengecase.domain.model.CaseStatus
import ru.itech.sbertrack.platform.challengecase.domain.port.CaseDataPort
import ru.itech.sbertrack.platform.common.exception.ApiException
import java.util.UUID
import kotlin.test.*

@SpringBootTest
@AutoConfigureMockMvc
class AgentControllerIntegrationTest {
    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var json: ObjectMapper
    @Autowired lateinit var agents: AgentDataPort
    @Autowired lateinit var cases: CaseDataPort
    @MockitoBean lateinit var gateway: AgentGatewayPort

    private fun login(): String {
        val result = mvc.perform(post("/api/v1/auth/sign-in").contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"student@example.com","password":"password"}"""))
            .andExpect(status().isOk).andReturn().response.contentAsString
        return "Bearer " + json.readTree(result)["token"].asText()
    }
    private fun create(token: String): String {
        val agent = agents.listAgents().first { it.status == AgentStatus.ACTIVE }
        val case = cases.list().first { it.status == CaseStatus.PUBLISHED }
        val result = mvc.perform(post("/api/v1/agents/sessions").header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON).content("""{"agentId":"${agent.id}","caseId":"${case.id}"}"""))
            .andExpect(status().isOk).andReturn().response.contentAsString
        return json.readTree(result)["id"].asText()
    }
    private fun send(id: String, token: String, content: String = "question") = mvc.perform(
        post("/api/v1/agents/sessions/$id/messages").header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(mapOf("content" to content))))

    @Test fun `session routes require authentication`() {
        val id = UUID.randomUUID()
        mvc.perform(get("/api/v1/agents/sessions/$id")).andExpect(status().isUnauthorized)
        mvc.perform(get("/api/v1/agents/sessions/latest").param("agentId", id.toString())).andExpect(status().isUnauthorized)
        mvc.perform(post("/api/v1/agents/sessions").contentType(MediaType.APPLICATION_JSON)
            .content("""{"agentId":"$id"}""")).andExpect(status().isUnauthorized)
        send(id.toString(), "").andExpect(status().isUnauthorized)
        verifyNoInteractions(gateway)
    }

    @Test fun `owner can send restore and continue without duplicated latest turn`() {
        val token = login(); val id = create(token)
        val captured = mutableListOf<AgentGenerationRequest>()
        doAnswer { invocation -> captured.add(invocation.getArgument(0)); "answer" }.`when`(gateway).generateAssistantResponse(any() ?: AgentGenerationRequest("", emptyList()))
        send(id, token, "first-question").andExpect(status().isOk)
        send(id, token, "second-question").andExpect(status().isOk)
        val restored = mvc.perform(get("/api/v1/agents/sessions/$id").header("Authorization", token))
            .andExpect(status().isOk).andReturn().response.contentAsString
        assertEquals(4, json.readTree(restored)["messages"].size())
        assertEquals(1, captured[0].messages.size); assertEquals(3, captured[1].messages.size)
        assertEquals(1, captured[1].messages.count { it.content.contains("second-question") })
    }

    @Test fun `provider failure leaves history unchanged and retry succeeds`() {
        val token = login(); val id = create(token)
        doThrow(ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Unavailable")).`when`(gateway).generateAssistantResponse(any() ?: AgentGenerationRequest("", emptyList()))
        send(id, token).andExpect(status().isServiceUnavailable)
        assertTrue(agents.findSessionById(UUID.fromString(id))!!.messages.isEmpty())
        doReturn("recovered").`when`(gateway).generateAssistantResponse(any() ?: AgentGenerationRequest("", emptyList()))
        send(id, token).andExpect(status().isOk)
        assertEquals(2, agents.findSessionById(UUID.fromString(id))!!.messages.size)
    }

    @Test fun `history changed during generation returns conflict without saving stale exchange`() {
        val token = login(); val id = create(token); val sessionId = UUID.fromString(id)
        doAnswer {
            agents.appendExchange(sessionId, 0,
                AgentMessage(sessionId = sessionId, role = AgentMessageRole.USER, content = "parallel question"),
                AgentMessage(sessionId = sessionId, role = AgentMessageRole.AGENT, content = "parallel answer"),
            )
            "stale answer"
        }.`when`(gateway).generateAssistantResponse(any() ?: AgentGenerationRequest("", emptyList()))
        send(id, token, "my question").andExpect(status().isConflict)
        assertEquals(listOf("parallel question", "parallel answer"), agents.findSessionById(sessionId)!!.messages.map { it.content })
        val requests = mutableListOf<AgentGenerationRequest>()
        doAnswer { invocation -> requests.add(invocation.getArgument(0)); "retry answer" }.`when`(gateway)
            .generateAssistantResponse(any() ?: AgentGenerationRequest("", emptyList()))
        send(id, token, "my question").andExpect(status().isOk)
        assertEquals(3, requests.single().messages.size)
        assertEquals(listOf("parallel question", "parallel answer", "my question", "retry answer"),
            agents.findSessionById(sessionId)!!.messages.map { it.content })
    }

    @Test fun `student cannot create read or send on behalf of another user`() {
        val owner = login(); val id = create(owner)
        val signedUp = mvc.perform(post("/api/v1/auth/sign-up").contentType(MediaType.APPLICATION_JSON)
            .content("""{"fullName":"Agent test","email":"agent-${UUID.randomUUID()}@example.test","password":"test-password","role":"STUDENT","studentType":"UNIVERSITY_STUDENT"}"""))
            .andExpect(status().isOk).andReturn().response.contentAsString
        val other = "Bearer " + json.readTree(signedUp)["token"].asText()
        mvc.perform(get("/api/v1/agents/sessions/$id").header("Authorization", other)).andExpect(status().isForbidden)
        send(id, other).andExpect(status().isForbidden)
        val session = agents.findSessionById(UUID.fromString(id))!!
        mvc.perform(post("/api/v1/agents/sessions").header("Authorization", other).contentType(MediaType.APPLICATION_JSON)
            .content("""{"agentId":"${session.agentId}","studentId":"${session.studentId}"}"""))
            .andExpect(status().isForbidden)
        verifyNoInteractions(gateway)
    }

    @Test fun `inactive agent cannot start or continue chat`() {
        val token = login(); val id = create(token)
        val session = agents.findSessionById(UUID.fromString(id))!!
        val agent = agents.findAgentById(session.agentId)!!
        try {
            agents.saveAgent(agent.copy(status = AgentStatus.DISABLED))
            send(id, token).andExpect(status().isForbidden)
            mvc.perform(post("/api/v1/agents/sessions").header("Authorization", token).contentType(MediaType.APPLICATION_JSON)
                .content("""{"agentId":"${agent.id}"}""")).andExpect(status().isForbidden)
            verifyNoInteractions(gateway)
        } finally { agents.saveAgent(agent) }
    }
}
