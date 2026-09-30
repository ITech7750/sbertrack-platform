package ru.itech.sbertrack.platform.agent.infrastructure.persistence

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.agent.domain.model.AgentDefinition
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessage
import ru.itech.sbertrack.platform.agent.domain.model.AgentSession
import ru.itech.sbertrack.platform.agent.domain.model.MasterPrompt

@Component
class AgentEntityMapper {
    fun toDomain(entity: AgentEntity): AgentDefinition =
        AgentDefinition(
            id = entity.id,
            code = entity.code,
            name = entity.name,
            description = entity.description,
            specialization = entity.specialization,
            status = entity.status,
            masterPromptId = entity.masterPromptId,
            capabilities = entity.capabilities,
        )

    fun toEntity(agent: AgentDefinition): AgentEntity =
        AgentEntity(
            id = agent.id,
            code = agent.code,
            name = agent.name,
            description = agent.description,
            specialization = agent.specialization,
            status = agent.status,
            masterPromptId = agent.masterPromptId,
            capabilities = agent.capabilities,
        )

    fun toDomain(entity: MasterPromptEntity): MasterPrompt =
        MasterPrompt(
            id = entity.id,
            agentId = entity.agentId,
            title = entity.title,
            promptText = entity.promptText,
            version = entity.version,
            status = entity.status,
            createdBy = entity.createdBy,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(prompt: MasterPrompt): MasterPromptEntity =
        MasterPromptEntity(
            id = prompt.id,
            agentId = prompt.agentId,
            title = prompt.title,
            promptText = prompt.promptText,
            version = prompt.version,
            status = prompt.status,
            createdBy = prompt.createdBy,
            updatedAt = prompt.updatedAt,
        )

    fun toDomain(entity: AgentSessionEntity, messages: List<AgentMessage>): AgentSession =
        AgentSession(
            id = entity.id,
            studentId = entity.studentId,
            caseId = entity.caseId,
            agentId = entity.agentId,
            messages = messages,
            createdAt = entity.createdAt,
        )

    fun toEntity(session: AgentSession): AgentSessionEntity =
        AgentSessionEntity(
            id = session.id,
            studentId = session.studentId,
            caseId = session.caseId,
            agentId = session.agentId,
            createdAt = session.createdAt,
        )

    fun toDomain(entity: AgentMessageEntity): AgentMessage =
        AgentMessage(
            id = entity.id,
            sessionId = entity.sessionId,
            role = entity.role,
            content = entity.content,
            createdAt = entity.createdAt,
        )

    fun toEntity(message: AgentMessage, sequenceNumber: Long): AgentMessageEntity =
        AgentMessageEntity(
            id = message.id,
            sessionId = message.sessionId,
            role = message.role,
            content = message.content,
            createdAt = message.createdAt,
            sequenceNumber = sequenceNumber,
        )
}
