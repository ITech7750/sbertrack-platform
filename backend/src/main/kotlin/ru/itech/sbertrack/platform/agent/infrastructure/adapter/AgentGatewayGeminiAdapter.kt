package ru.itech.sbertrack.platform.agent.infrastructure.adapter

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Component
import org.springframework.web.util.UriUtils
import ru.itech.sbertrack.platform.agent.domain.model.AgentGenerationRequest
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessageRole
import java.nio.charset.StandardCharsets

@Component
class AgentGatewayGeminiAdapter : ModelProtocolAdapter {
    override val protocol = ModelProtocol.GEMINI
    override fun path(profile: ModelProfile) = "/models/" +
        UriUtils.encodePathSegment(profile.model.removePrefix("models/"), StandardCharsets.UTF_8) + ":generateContent"
    override fun headers(profile: ModelProfile) = mapOf("x-goog-api-key" to profile.apiKey)
    override fun body(profile: ModelProfile, request: AgentGenerationRequest) = mapOf(
        "systemInstruction" to mapOf("parts" to listOf(mapOf("text" to request.systemPrompt))),
        "contents" to request.messages.map {
            mapOf("role" to if (it.role == AgentMessageRole.USER) "user" else "model",
                "parts" to listOf(mapOf("text" to it.content)))
        },
        "generationConfig" to mapOf("maxOutputTokens" to profile.maxOutputTokens),
    )
    override fun text(response: JsonNode): String = response.path("candidates").path(0).path("content").path("parts")
        .filter { !it.path("thought").asBoolean(false) }.joinToString("\n") { it.path("text").asText("") }
}
