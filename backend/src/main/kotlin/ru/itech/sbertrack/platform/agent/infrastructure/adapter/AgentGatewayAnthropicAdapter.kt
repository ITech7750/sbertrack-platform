package ru.itech.sbertrack.platform.agent.infrastructure.adapter

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.agent.domain.model.AgentGenerationRequest
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessageRole

@Component
class AgentGatewayAnthropicAdapter : ModelProtocolAdapter {
    override val protocol = ModelProtocol.ANTHROPIC
    override fun path(profile: ModelProfile) = "/messages"
    override fun headers(profile: ModelProfile) = mapOf(
        "x-api-key" to profile.apiKey, "anthropic-version" to "2023-06-01",
    )
    override fun body(profile: ModelProfile, request: AgentGenerationRequest) = mapOf(
        "model" to profile.model, "max_tokens" to profile.maxOutputTokens,
        "system" to request.systemPrompt,
        "messages" to request.messages.map {
            mapOf("role" to if (it.role == AgentMessageRole.USER) "user" else "assistant", "content" to it.content)
        },
    )
    override fun text(response: JsonNode): String = response.path("content")
        .filter { it.path("type").asText() == "text" }.joinToString("\n") { it.path("text").asText("") }
}
