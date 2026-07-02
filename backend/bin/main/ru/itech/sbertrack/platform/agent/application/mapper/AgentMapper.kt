package ru.itech.sbertrack.platform.agent.application.mapper

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.agent.domain.model.AgentDefinition
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessage
import ru.itech.sbertrack.platform.agent.domain.model.AgentSession
import ru.itech.sbertrack.platform.agent.domain.model.MasterPrompt
import ru.itech.sbertrack.platform.agent.dto.response.AgentDefinitionResponse
import ru.itech.sbertrack.platform.agent.dto.response.AgentMessageResponse
import ru.itech.sbertrack.platform.agent.dto.response.AgentSessionResponse
import ru.itech.sbertrack.platform.agent.dto.response.MasterPromptResponse

@Component
class AgentMapper {
    fun toResponse(agentDefinition: AgentDefinition): AgentDefinitionResponse =
        AgentDefinitionResponse(
            id = agentDefinition.id,
            code = agentDefinition.code,
            name = agentDefinition.name,
            description = agentDefinition.description,
            specialization = agentDefinition.specialization,
            status = agentDefinition.status,
            masterPromptId = agentDefinition.masterPromptId,
            capabilities = agentDefinition.capabilities,
        )

    fun toResponse(masterPrompt: MasterPrompt): MasterPromptResponse =
        MasterPromptResponse(
            id = masterPrompt.id,
            agentId = masterPrompt.agentId,
            title = masterPrompt.title,
            promptText = masterPrompt.promptText,
            version = masterPrompt.version,
            status = masterPrompt.status,
            createdBy = masterPrompt.createdBy,
            updatedAt = masterPrompt.updatedAt,
        )

    fun toResponse(agentSession: AgentSession): AgentSessionResponse =
        AgentSessionResponse(
            id = agentSession.id,
            studentId = agentSession.studentId,
            caseId = agentSession.caseId,
            agentId = agentSession.agentId,
            messages = agentSession.messages.map(::toResponse),
            createdAt = agentSession.createdAt,
        )

    private fun toResponse(agentMessage: AgentMessage): AgentMessageResponse =
        AgentMessageResponse(
            id = agentMessage.id,
            sessionId = agentMessage.sessionId,
            role = agentMessage.role,
            content = agentMessage.content,
            createdAt = agentMessage.createdAt,
        )
}
