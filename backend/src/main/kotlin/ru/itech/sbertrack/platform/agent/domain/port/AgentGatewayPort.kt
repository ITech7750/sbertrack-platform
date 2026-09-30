package ru.itech.sbertrack.platform.agent.domain.port

import ru.itech.sbertrack.platform.agent.domain.model.AgentGenerationRequest

interface AgentGatewayPort {
    fun generateAssistantResponse(request: AgentGenerationRequest): String
}
