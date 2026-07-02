package ru.itech.sbertrack.platform.agent.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.util.UUID

@Schema(description = "Создание мастер-промпта")
data class MasterPromptCreateRequest(
    val agentId: UUID,
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val promptText: String,
    val createdBy: UUID?,
)
