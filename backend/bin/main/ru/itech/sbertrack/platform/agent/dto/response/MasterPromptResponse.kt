package ru.itech.sbertrack.platform.agent.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.agent.domain.model.MasterPromptStatus
import java.time.Instant
import java.util.UUID

@Schema(description = "Мастер-промпт агента")
data class MasterPromptResponse(
    val id: UUID,
    val agentId: UUID,
    val title: String,
    val promptText: String,
    val version: Int,
    val status: MasterPromptStatus,
    val createdBy: UUID,
    val updatedAt: Instant,
)
