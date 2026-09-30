package ru.itech.sbertrack.platform.agent.infrastructure.adapter

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.agent.domain.model.AgentGenerationRequest
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessageRole

@Component
class AgentGatewayCompatibleAdapter : ModelProtocolAdapter {
    override val protocol = ModelProtocol.OPENAI_COMPATIBLE
    override fun path(profile: ModelProfile) = "/chat/completions"
    override fun headers(profile: ModelProfile) = if (profile.apiKey.isBlank()) emptyMap()
        else mapOf("Authorization" to "Bearer ${profile.apiKey}")
    override fun body(profile: ModelProfile, request: AgentGenerationRequest) = mapOf(
        "model" to profile.model, profile.tokenLimitField to profile.maxOutputTokens, "stream" to false,
        "messages" to listOf(mapOf("role" to "system", "content" to request.systemPrompt)) + request.messages.map {
            mapOf("role" to if (it.role == AgentMessageRole.USER) "user" else "assistant", "content" to it.content)
        },
    )
    override fun text(response: JsonNode): String = response.path("choices").path(0).path("message").path("content")
        .takeIf { it.isTextual }?.asText() ?: ""
}
