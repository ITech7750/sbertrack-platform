package ru.itech.sbertrack.platform.agent.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "Сессия агента")
data class AgentSessionResponse(
    val id: UUID,
    val studentId: UUID,
    val caseId: UUID?,
    val agentId: UUID,
    val messages: List<AgentMessageResponse>,
    val createdAt: Instant,
)
