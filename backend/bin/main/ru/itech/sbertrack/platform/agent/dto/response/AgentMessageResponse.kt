package ru.itech.sbertrack.platform.agent.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessageRole
import java.time.Instant
import java.util.UUID

@Schema(description = "Сообщение в сессии агента")
data class AgentMessageResponse(
    val id: UUID,
    val sessionId: UUID,
    val role: AgentMessageRole,
    val content: String,
    val createdAt: Instant,
)
