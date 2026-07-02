package ru.itech.sbertrack.platform.agent.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.agent.domain.model.AgentCapability
import ru.itech.sbertrack.platform.agent.domain.model.AgentSpecialization
import ru.itech.sbertrack.platform.agent.domain.model.AgentStatus
import java.util.UUID

@Schema(description = "Определение агента")
data class AgentDefinitionResponse(
    val id: UUID,
    val code: String,
    val name: String,
    val description: String,
    val specialization: AgentSpecialization,
    val status: AgentStatus,
    val masterPromptId: UUID?,
    val capabilities: List<AgentCapability>,
)
