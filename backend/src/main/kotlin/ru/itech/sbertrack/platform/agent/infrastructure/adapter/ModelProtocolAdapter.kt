package ru.itech.sbertrack.platform.agent.infrastructure.adapter

import com.fasterxml.jackson.databind.JsonNode
import ru.itech.sbertrack.platform.agent.domain.model.AgentGenerationRequest

interface ModelProtocolAdapter {
    val protocol: ModelProtocol
    fun path(profile: ModelProfile): String
    fun headers(profile: ModelProfile): Map<String, String>
    fun body(profile: ModelProfile, request: AgentGenerationRequest): Any
    fun text(response: JsonNode): String
}
