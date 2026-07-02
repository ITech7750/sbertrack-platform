package ru.itech.sbertrack.platform.agent.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Сообщение пользователя агенту")
data class AgentMessageRequest(
    @field:NotBlank
    val content: String,
    val caseTitle: String? = null,
    val artifacts: List<String> = emptyList(),
)
