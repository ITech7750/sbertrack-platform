package ru.itech.sbertrack.platform.agent.domain.port

import ru.itech.sbertrack.platform.agent.domain.model.AgentDefinition
import ru.itech.sbertrack.platform.agent.domain.model.AgentSession
import ru.itech.sbertrack.platform.agent.domain.model.MasterPrompt
import java.util.UUID

interface AgentDataPort {
    fun listAgents(): List<AgentDefinition>
    fun findAgentById(id: UUID): AgentDefinition?
    fun saveAgent(agentDefinition: AgentDefinition): AgentDefinition
    fun listMasterPrompts(): List<MasterPrompt>
    fun findMasterPromptById(id: UUID): MasterPrompt?
    fun saveMasterPrompt(masterPrompt: MasterPrompt): MasterPrompt
    fun findSessionById(id: UUID): AgentSession?
    fun saveSession(agentSession: AgentSession): AgentSession
}
