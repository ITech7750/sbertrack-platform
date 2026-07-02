package ru.itech.sbertrack.platform.agent.domain.model

import java.util.UUID

data class AgentDefinition(
    val id: UUID = UUID.randomUUID(),
    val code: String,
    val name: String,
    val description: String,
    val specialization: AgentSpecialization,
    val status: AgentStatus,
    val masterPromptId: UUID?,
    val capabilities: List<AgentCapability>,
)
