package ru.itech.sbertrack.platform.agent.infrastructure.persistence

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.http.HttpStatus
import ru.itech.sbertrack.platform.agent.domain.model.AgentDefinition
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessage
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessageRole
import ru.itech.sbertrack.platform.agent.domain.model.AgentSession
import ru.itech.sbertrack.platform.agent.domain.model.MasterPrompt
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.common.exception.ApiException
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import java.util.UUID

@Component
class AgentJpaAdapter(
    private val agentRepository: AgentJpaRepository,
    private val masterPromptRepository: MasterPromptJpaRepository,
    private val sessionRepository: AgentSessionJpaRepository,
    private val messageRepository: AgentMessageJpaRepository,
    private val mapper: AgentEntityMapper,
) : AgentDataPort {
    override fun listAgents(): List<AgentDefinition> =
        agentRepository.findAll().map(mapper::toDomain)

    override fun findAgentById(id: UUID): AgentDefinition? =
        agentRepository.findById(id).orElse(null)?.let(mapper::toDomain)

    override fun saveAgent(agentDefinition: AgentDefinition): AgentDefinition {
        agentRepository.save(mapper.toEntity(agentDefinition))
        return agentDefinition
    }

    override fun listMasterPrompts(): List<MasterPrompt> =
        masterPromptRepository.findAll().map(mapper::toDomain)

    override fun findMasterPromptById(id: UUID): MasterPrompt? =
        masterPromptRepository.findById(id).orElse(null)?.let(mapper::toDomain)

    override fun saveMasterPrompt(masterPrompt: MasterPrompt): MasterPrompt {
        masterPromptRepository.save(mapper.toEntity(masterPrompt))
        return masterPrompt
    }

    override fun findSessionById(id: UUID): AgentSession? =
        sessionRepository.findById(id).orElse(null)?.let { entity ->
            val messages = messageRepository.findBySessionIdOrderBySequenceNumber(entity.id).map(mapper::toDomain)
            mapper.toDomain(entity, messages)
        }

    override fun findLatestSession(studentId: UUID, agentId: UUID, caseId: UUID?): AgentSession? {
        val entity = if (caseId != null) {
            sessionRepository.findFirstByStudentIdAndAgentIdAndCaseIdOrderByCreatedAtDesc(studentId, agentId, caseId)
        } else {
            sessionRepository.findFirstByStudentIdAndAgentIdAndCaseIdIsNullOrderByCreatedAtDesc(studentId, agentId)
        }
        return entity?.let {
            val messages = messageRepository.findBySessionIdOrderBySequenceNumber(it.id).map(mapper::toDomain)
            mapper.toDomain(it, messages)
        }
    }

    @Transactional
    override fun saveSession(agentSession: AgentSession): AgentSession {
        require(agentSession.messages.isEmpty()) { "Use appendExchange to persist messages" }
        sessionRepository.save(mapper.toEntity(agentSession))
        return agentSession
    }

    @Transactional
    override fun appendExchange(sessionId: UUID, expectedMessageCount: Int, user: AgentMessage, assistant: AgentMessage): AgentSession {
        require(user.sessionId == sessionId && assistant.sessionId == sessionId)
        require(user.role == AgentMessageRole.USER && assistant.role == AgentMessageRole.AGENT)
        val entity = sessionRepository.findForAppend(sessionId) ?: throw NotFoundException("Сессия агента не найдена")
        val history = messageRepository.findBySessionIdOrderBySequenceNumber(sessionId)
        if (history.size != expectedMessageCount) throw ApiException(HttpStatus.CONFLICT,
            "Диалог обновился в другом запросе. Сообщение не сохранено; отправьте его ещё раз.")
        val next = (history.lastOrNull()?.sequenceNumber ?: 0L) + 1
        val appended = listOf(mapper.toEntity(user, next), mapper.toEntity(assistant, next + 1))
        messageRepository.saveAllAndFlush(appended)
        return mapper.toDomain(entity, (history + appended).map(mapper::toDomain))
    }
}
