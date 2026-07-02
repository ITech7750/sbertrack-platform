package ru.itech.sbertrack.platform.agent.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Обновление мастер-промпта")
data class MasterPromptUpdateRequest(
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val promptText: String,
)
