package ru.itech.sbertrack.platform.agent.domain.port

import ru.itech.sbertrack.platform.agent.domain.model.AgentDefinition
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessage

interface AgentGatewayPort {
    fun generateAssistantResponse(agentDefinition: AgentDefinition, history: List<AgentMessage>, userMessage: String): String
}
