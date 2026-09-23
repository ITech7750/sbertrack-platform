package ru.itech.sbertrack.platform.agent.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Сообщение пользователя агенту")
data class AgentMessageRequest(
    @field:NotBlank
    @field:Size(max = 8000)
    val content: String,
    @field:Size(max = 500)
    val caseTitle: String? = null,
    @field:Size(max = 20)
    val artifacts: List<String> = emptyList(),
)
