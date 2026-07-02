package ru.itech.sbertrack.platform.agent.infrastructure.adapter.mistral

import com.fasterxml.jackson.annotation.JsonProperty

data class MistralChatRequest(
    val model: String,
    val messages: List<MistralMessage>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 2048
)

data class MistralMessage(
    val role: String,
    val content: String
)

data class MistralChatResponse(
    val choices: List<MistralChoice>
)

data class MistralChoice(
    val message: MistralMessage
)
