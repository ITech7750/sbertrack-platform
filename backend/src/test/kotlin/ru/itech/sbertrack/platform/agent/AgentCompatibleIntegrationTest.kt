package ru.itech.sbertrack.platform.agent

import com.sun.net.httpserver.HttpServer
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.itech.sbertrack.platform.agent.domain.model.AgentStatus
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.*

/** Complete controller -> request builder -> configured HTTP adapter -> persistence path. */
@SpringBootTest
@AutoConfigureMockMvc
class AgentCompatibleIntegrationTest {
    @Autowired lateinit var mvc: MockMvc
    @Autowired lateinit var json: ObjectMapper
    @Autowired lateinit var agents: AgentDataPort

    companion object {
        val requests = CopyOnWriteArrayList<String>()
        val server: HttpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/v1/chat/completions") { exchange ->
                requests.add(exchange.requestBody.bufferedReader().readText())
                val bytes = """{"choices":[{"message":{"content":"HTTP mentor answer"}}]}""".toByteArray()
                exchange.responseHeaders.set("Content-Type", "application/json")
                exchange.sendResponseHeaders(200, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            start()
        }
        @JvmStatic @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("sbertrack.agent.active-profile") { "local" }
            registry.add("sbertrack.agent.fallback-profile") { "" }
            registry.add("sbertrack.agent.profiles.local.base-url") { "http://127.0.0.1:${server.address.port}/v1" }
            registry.add("sbertrack.agent.profiles.local.model") { "local-test-model" }
            registry.add("sbertrack.agent.profiles.local.api-key") { "" }
        }
        @JvmStatic @AfterAll fun stop() { server.stop(0) }
    }

    @Test fun `configured compatible profile returns and stores HTTP answer`() {
        val signedIn = mvc.perform(post("/api/v1/auth/sign-in").contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"student@example.com","password":"password"}"""))
            .andExpect(status().isOk).andReturn().response.contentAsString
        val token = "Bearer " + json.readTree(signedIn)["token"].asText()
        val agent = agents.listAgents().first { it.status == AgentStatus.ACTIVE }
        val created = mvc.perform(post("/api/v1/agents/sessions").header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON).content("""{"agentId":"${agent.id}"}"""))
            .andExpect(status().isOk).andReturn().response.contentAsString
        val id = json.readTree(created)["id"].asText()
        val response = mvc.perform(post("/api/v1/agents/sessions/$id/messages").header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON).content("""{"content":"My HTTP question"}"""))
            .andExpect(status().isOk).andReturn().response.contentAsString
        assertEquals("HTTP mentor answer", json.readTree(response)["messages"][1]["content"].asText())
        val sent = json.readTree(requests.single())
        assertEquals("local-test-model", sent["model"].asText())
        assertEquals(2, sent["messages"].size())
        assertEquals("My HTTP question", sent["messages"][1]["content"].asText())
        val restored = mvc.perform(get("/api/v1/agents/sessions/$id").header("Authorization", token))
            .andExpect(status().isOk).andReturn().response.contentAsString
        assertEquals(2, json.readTree(restored)["messages"].size())
    }
}
