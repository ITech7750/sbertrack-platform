package ru.itech.sbertrack.platform.agent.infrastructure.adapter

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import ru.itech.sbertrack.platform.agent.domain.model.AgentGenerationRequest
import ru.itech.sbertrack.platform.agent.domain.port.AgentGatewayPort
import ru.itech.sbertrack.platform.common.exception.ApiException
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.time.Duration
import java.util.concurrent.CancellationException

@Component
class ProfileAgentGateway(
    private val properties: AgentGatewayProperties,
    adapters: List<ModelProtocolAdapter>,
) : AgentGatewayPort {
    private val log = LoggerFactory.getLogger(javaClass)
    private val json = jacksonObjectMapper()
    private val adapters = adapters.associateBy { it.protocol }
    private val clients: Map<String, RestClient>

    init {
        properties.validate()
        clients = listOf(properties.activeProfile, properties.fallbackProfile).filter(String::isNotBlank)
            .filter { properties.profiles.getValue(it).protocol != ModelProtocol.MOCK }.associateWith { name ->
                val profile = properties.profiles.getValue(name)
                require(profile.protocol in this.adapters) { "Unsupported agent protocol" }
                val http = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(profile.connectTimeoutMs.toLong()))
                    .followRedirects(HttpClient.Redirect.NEVER).build()
                val factory = JdkClientHttpRequestFactory(http)
                factory.setReadTimeout(Duration.ofMillis(profile.readTimeoutMs.toLong()))
                RestClient.builder().requestFactory(factory).build()
            }
    }

    override fun generateAssistantResponse(request: AgentGenerationRequest): String {
        for (name in listOf(properties.activeProfile, properties.fallbackProfile).filter(String::isNotBlank)) {
            if (Thread.currentThread().isInterrupted) throw unavailable()
            val profile = properties.profiles.getValue(name)
            if (profile.protocol == ModelProtocol.MOCK) {
                return "Демо-наставник: какие предположения лежат в основе вашего подхода и как вы их проверите?"
            }
            val adapter = adapters.getValue(profile.protocol)
            for (attempt in 1..profile.maxAttempts) {
                if (Thread.currentThread().isInterrupted) throw unavailable()
                val start = System.nanoTime()
                try {
                    val bytes = clients.getValue(name).post()
                        .uri(URI(profile.baseUrl.trimEnd('/') + adapter.path(profile)))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .headers { headers -> adapter.headers(profile).forEach { (key, value) -> headers.set(key, value) } }
                        .body(adapter.body(profile, request)).retrieve().body(ByteArray::class.java)
                    // Read the complete HTTP body before parsing: Jackson exceptions also extend
                    // IOException and must not be confused with a retryable transport failure.
                    val response = try { bytes?.let(json::readTree) } catch (_: JsonProcessingException) {
                        log.warn("agent profile={} model={} attempt={} result=invalid_json durationMs={}", name, profile.model, attempt, elapsed(start))
                        throw unavailable()
                    }
                    val text = response?.let(adapter::text)?.trim().orEmpty()
                    if (text.isEmpty()) {
                        log.warn("agent profile={} model={} attempt={} result=empty durationMs={}", name, profile.model, attempt, elapsed(start))
                        // A refusal/empty answer must not be bypassed by another provider.
                        throw unavailable()
                    }
                    log.info("agent profile={} model={} attempt={} result=ok durationMs={}", name, profile.model, attempt, elapsed(start))
                    return text
                } catch (exception: RuntimeException) {
                    if (exception !is RestClientException && exception !is CancellationException) throw exception
                    if (Thread.currentThread().isInterrupted) throw unavailable()
                    val status = (exception as? RestClientResponseException)?.statusCode?.value()
                    // JDK timeouts can cancel the response future; body I/O failures are wrapped
                    // in RestClientException rather than necessarily ResourceAccessException.
                    val retryable = if (status != null) status in setOf(429, 500, 502, 503, 504)
                        else generateSequence<Throwable>(exception) { it.cause }.any {
                            it is IOException || it is CancellationException
                        }
                    log.warn("agent profile={} model={} attempt={} result={} durationMs={}", name, profile.model, attempt,
                        status?.toString() ?: "transport_or_format_error", elapsed(start))
                    if (!retryable) throw unavailable()
                    if (attempt < profile.maxAttempts) {
                        try { Thread.sleep(profile.retryDelayMs) } catch (_: InterruptedException) {
                            Thread.currentThread().interrupt()
                            throw unavailable()
                        }
                    }
                }
            }
        }
        throw unavailable()
    }

    private fun elapsed(start: Long) = (System.nanoTime() - start) / 1_000_000
    private fun unavailable() = ApiException(HttpStatus.SERVICE_UNAVAILABLE,
        "Наставник временно недоступен. Сообщение не сохранено; попробуйте отправить его позже.")
}
