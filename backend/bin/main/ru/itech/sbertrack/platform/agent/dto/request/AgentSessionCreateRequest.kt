package ru.itech.sbertrack.platform.agent.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Создание сессии агента")
data class AgentSessionCreateRequest(
    val studentId: UUID?,
    val caseId: UUID?,
    val agentId: UUID,
)
