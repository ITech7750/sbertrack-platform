package ru.itech.sbertrack.platform.agent

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import ru.itech.sbertrack.platform.agent.domain.model.*
import ru.itech.sbertrack.platform.agent.infrastructure.adapter.*
import ru.itech.sbertrack.platform.common.exception.ApiException
import java.net.InetSocketAddress
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.*

class AgentGatewayTest {
    private val json = jacksonObjectMapper()
    private val requests = CopyOnWriteArrayList<Triple<String, Map<String, List<String>>, String>>()
    private val executor = Executors.newCachedThreadPool()
    private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
        this.executor = this@AgentGatewayTest.executor
        start()
    }
    private val request = AgentGenerationRequest("system rules", listOf(
        GenerationMessage(AgentMessageRole.USER, "old question"),
        GenerationMessage(AgentMessageRole.AGENT, "old answer"),
        GenerationMessage(AgentMessageRole.USER, "new question"),
    ))

    @AfterEach fun stop() { server.stop(0); executor.shutdownNow() }

    private fun endpoint(path: String, status: (Int) -> Int = { 200 }, delay: Long = 0, response: String) {
        server.createContext(path) { exchange ->
            requests.add(Triple(exchange.requestURI.toString(), exchange.requestHeaders.toMap(),
                exchange.requestBody.bufferedReader().readText()))
            if (delay > 0) Thread.sleep(delay)
            val bytes = response.toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(status(requests.size), bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
    }
    private fun profile(protocol: ModelProtocol, path: String = "/v1") = ModelProfile().apply {
        this.protocol = protocol; baseUrl = "http://127.0.0.1:${server.address.port}$path"
        model = "test-model"; apiKey = "test-secret"; retryDelayMs = 0
    }
    private fun gateway(primary: ModelProfile, fallback: ModelProfile? = null): ProfileAgentGateway {
        val properties = AgentGatewayProperties().apply {
            activeProfile = "primary"
            profiles = if (fallback == null) mapOf("primary" to primary) else mapOf("primary" to primary, "backup" to fallback)
            fallbackProfile = if (fallback == null) "" else "backup"
        }
        return ProfileAgentGateway(properties, listOf(AgentGatewayCompatibleAdapter(), AgentGatewayAnthropicAdapter(), AgentGatewayGeminiAdapter()))
    }

    @ParameterizedTest
    @EnumSource(value = ModelProtocol::class, names = ["ANTHROPIC", "GEMINI", "OPENAI_COMPATIBLE"])
    fun `wire contracts carry system history latest message once and output budget`(protocol: ModelProtocol) {
        val path = when (protocol) {
            ModelProtocol.ANTHROPIC -> "/v1/messages"
            ModelProtocol.GEMINI -> "/v1/models/test-model:generateContent"
            else -> "/v1/chat/completions"
        }
        val response = when (protocol) {
            ModelProtocol.ANTHROPIC -> """{"content":[{"type":"text","text":"answer"}]}"""
            ModelProtocol.GEMINI -> """{"candidates":[{"content":{"parts":[{"thought":true,"text":"hidden"},{"text":"answer"}]}}]}"""
            else -> """{"choices":[{"message":{"content":"answer"}}]}"""
        }
        endpoint(path, response = response)
        assertEquals("answer", gateway(profile(protocol)).generateAssistantResponse(request))
        val sent = requests.single()
        assertEquals(path, sent.first)
        assertEquals(1, Regex("new question").findAll(sent.third).count())
        assertTrue(sent.third.contains("system rules"))
        assertFalse(sent.first.contains("test-secret"))
        val body = json.readTree(sent.third)
        when (protocol) {
            ModelProtocol.ANTHROPIC -> {
                assertEquals(3, body["messages"].size()); assertEquals(1024, body["max_tokens"].asInt())
                assertEquals("test-secret", sent.second.entries.first { it.key.equals("x-api-key", true) }.value.single())
            }
            ModelProtocol.GEMINI -> {
                assertEquals("model", body["contents"][1]["role"].asText())
                assertEquals(1024, body["generationConfig"]["maxOutputTokens"].asInt())
                assertTrue(sent.second.keys.any { it.equals("x-goog-api-key", true) })
            }
            else -> {
                assertEquals(4, body["messages"].size()); assertEquals(1024, body["max_tokens"].asInt())
                assertEquals("Bearer test-secret", sent.second.entries.first { it.key.equals("Authorization", true) }.value.single())
            }
        }
    }

    @Test fun `transient failure is retried`() {
        endpoint("/v1/chat/completions", status = { if (it == 1) 429 else 200 }, response = """{"choices":[{"message":{"content":"ok"}}]}""")
        assertEquals("ok", gateway(profile(ModelProtocol.OPENAI_COMPATIBLE)).generateAssistantResponse(request))
        assertEquals(2, requests.size)
    }

    @Test fun `explicit fallback runs only after attempts exhausted`() {
        endpoint("/v1/chat/completions", status = { 503 }, response = "{}")
        endpoint("/backup/chat/completions", response = """{"choices":[{"message":{"content":"backup"}}]}""")
        assertEquals("backup", gateway(profile(ModelProtocol.OPENAI_COMPATIBLE), profile(ModelProtocol.OPENAI_COMPATIBLE, "/backup"))
            .generateAssistantResponse(request))
        assertEquals(listOf("/v1/chat/completions", "/v1/chat/completions", "/backup/chat/completions"), requests.map { it.first })
    }

    @Test fun `authentication error never retries or falls back`() {
        endpoint("/v1/chat/completions", status = { 401 }, response = "secret provider error")
        val error = assertFailsWith<ApiException> {
            gateway(profile(ModelProtocol.OPENAI_COMPATIBLE), ModelProfile()).generateAssistantResponse(request)
        }
        assertEquals(503, error.httpStatus.value()); assertEquals(1, requests.size)
        assertFalse(error.message.contains("secret"))
    }

    @Test fun `empty response never becomes successful mock reply`() {
        endpoint("/v1/chat/completions", response = "{}")
        assertFailsWith<ApiException> { gateway(profile(ModelProtocol.OPENAI_COMPATIBLE), ModelProfile()).generateAssistantResponse(request) }
        assertEquals(1, requests.size)
    }

    @Test fun `read timeout is bounded and does not silently select mock`() {
        endpoint("/v1/chat/completions", delay = 1000, response = "{}")
        val p = profile(ModelProtocol.OPENAI_COMPATIBLE).apply { readTimeoutMs = 100; maxAttempts = 1 }
        assertFailsWith<ApiException> { gateway(p).generateAssistantResponse(request) }
        assertEquals(1, requests.size)
    }

    @Test fun `local server accepts no key and alternate token budget field`() {
        endpoint("/v1/chat/completions", response = """{"choices":[{"message":{"content":"local"}}]}""")
        val p = profile(ModelProtocol.OPENAI_COMPATIBLE).apply { apiKey = ""; tokenLimitField = "max_completion_tokens" }
        assertEquals("local", gateway(p).generateAssistantResponse(request))
        assertFalse(requests.single().second.keys.any { it.equals("Authorization", true) })
        assertEquals(1024, json.readTree(requests.single().third)["max_completion_tokens"].asInt())
    }

    private fun partialBodyEndpoint(path: String, stallFirstOnly: Boolean = false): AtomicInteger {
        val calls = AtomicInteger()
        server.createContext(path) { exchange ->
            val attempt = calls.incrementAndGet()
            exchange.requestBody.readAllBytes()
            val bytes = """{"choices":[{"message":{"content":"recovered"}}]}""".toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            try {
                exchange.responseBody.use { body ->
                    body.write(bytes, 0, 1)
                    body.flush()
                    if (!stallFirstOnly || attempt == 1) Thread.sleep(1500)
                    body.write(bytes, 1, bytes.size - 1)
                }
            } catch (_: java.io.IOException) {
                // The client's timeout closes the connection while this test server is stalled.
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            } finally { exchange.close() }
        }
        return calls
    }

    @Test fun `timeout after response headers retries body transport failure`() {
        val calls = partialBodyEndpoint("/v1/chat/completions", stallFirstOnly = true)
        val primary = profile(ModelProtocol.OPENAI_COMPATIBLE).apply { readTimeoutMs = 500 }
        assertEquals("recovered", gateway(primary).generateAssistantResponse(request))
        assertEquals(2, calls.get())
    }

    @Test fun `repeated timeout during body reading reaches explicit fallback`() {
        val calls = partialBodyEndpoint("/v1/chat/completions")
        endpoint("/backup/chat/completions", response = """{"choices":[{"message":{"content":"backup"}}]}""")
        val primary = profile(ModelProtocol.OPENAI_COMPATIBLE).apply { readTimeoutMs = 500 }
        assertEquals("backup", gateway(primary, profile(ModelProtocol.OPENAI_COMPATIBLE, "/backup"))
            .generateAssistantResponse(request))
        assertEquals(2, calls.get())
        assertEquals(listOf("/backup/chat/completions"), requests.map { it.first })
    }

    @ParameterizedTest
    @ValueSource(strings = ["{invalid json", "{\"choices\":["])
    fun `fully received malformed JSON does not retry or fall back`(body: String) {
        endpoint("/v1/chat/completions", response = body)
        endpoint("/backup/chat/completions", response = """{"choices":[{"message":{"content":"backup"}}]}""")
        val error = assertFailsWith<ApiException> {
            gateway(profile(ModelProtocol.OPENAI_COMPATIBLE), profile(ModelProtocol.OPENAI_COMPATIBLE, "/backup"))
                .generateAssistantResponse(request)
        }
        assertEquals(503, error.httpStatus.value())
        assertEquals(listOf("/v1/chat/completions"), requests.map { it.first })
    }

    @Test fun `connection closed before declared body length is retried`() {
        val calls = AtomicInteger()
        server.createContext("/v1/chat/completions") { exchange ->
            val attempt = calls.incrementAndGet()
            exchange.requestBody.readAllBytes()
            val bytes = """{"choices":[{"message":{"content":"recovered"}}]}""".toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, bytes.size.toLong())
            try {
                exchange.responseBody.use { body ->
                    body.write(bytes, 0, if (attempt == 1) 1 else bytes.size)
                }
            } catch (_: java.io.IOException) {
                // Deliberately violate Content-Length on the first response.
            } finally { exchange.close() }
        }
        val primary = profile(ModelProtocol.OPENAI_COMPATIBLE).apply { readTimeoutMs = 500 }
        assertEquals("recovered", gateway(primary).generateAssistantResponse(request))
        assertEquals(2, calls.get())
    }

    @Test fun `interrupted caller does not send or fall back and preserves interrupt flag`() {
        endpoint("/v1/chat/completions", response = "{}")
        val gateway = gateway(profile(ModelProtocol.OPENAI_COMPATIBLE), ModelProfile())
        Thread.currentThread().interrupt()
        try {
            assertFailsWith<ApiException> { gateway.generateAssistantResponse(request) }
            assertTrue(Thread.currentThread().isInterrupted)
            assertTrue(requests.isEmpty())
        } finally { Thread.interrupted() }
    }

    @Test fun `invalid selected configuration fails at startup`() {
        assertFailsWith<IllegalArgumentException> { gateway(ModelProfile().apply { protocol = ModelProtocol.OPENAI_COMPATIBLE }) }
        assertFailsWith<IllegalArgumentException> { AgentGatewayProperties().apply { activeProfile = "missing" }.validate() }
        assertFailsWith<IllegalArgumentException> { profile(ModelProtocol.GEMINI).apply { apiKey = "" }.validate() }
    }
}
