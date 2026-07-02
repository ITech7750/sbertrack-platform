package ru.itech.sbertrack.platform.agent.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.agent.application.mapper.AgentMapper
import ru.itech.sbertrack.platform.agent.domain.model.MasterPrompt
import ru.itech.sbertrack.platform.agent.domain.model.MasterPromptStatus
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.agent.dto.request.MasterPromptCreateRequest
import ru.itech.sbertrack.platform.agent.dto.request.MasterPromptUpdateRequest
import ru.itech.sbertrack.platform.agent.dto.response.MasterPromptResponse
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.user.domain.model.UserRole
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort
import java.util.UUID

@Service
class MasterPromptService(
    private val agentDataPort: AgentDataPort,
    private val userDataPort: UserDataPort,
    private val agentMapper: AgentMapper,
    private val authService: AuthService,
) {
    fun list(): List<MasterPromptResponse> =
        agentDataPort.listMasterPrompts().map(agentMapper::toResponse)

    fun get(id: UUID): MasterPromptResponse =
        findDomain(id).let(agentMapper::toResponse)

    fun create(request: MasterPromptCreateRequest, authorization: String?): MasterPromptResponse {
        agentDataPort.findAgentById(request.agentId) ?: throw NotFoundException("Агент не найден")
        val creatorId = request.createdBy
            ?: authService.tryCurrentUser(authorization)?.id
            ?: userDataPort.list().first { it.role == UserRole.MODERATOR }.id
        val prompt = MasterPrompt(
            agentId = request.agentId,
            title = request.title,
            promptText = request.promptText,
            version = 1,
            status = MasterPromptStatus.DRAFT,
            createdBy = creatorId,
        )
        return agentMapper.toResponse(agentDataPort.saveMasterPrompt(prompt))
    }

    fun update(id: UUID, request: MasterPromptUpdateRequest): MasterPromptResponse =
        agentMapper.toResponse(agentDataPort.saveMasterPrompt(findDomain(id).update(request.title, request.promptText)))

    fun activate(id: UUID): MasterPromptResponse {
        val active = agentDataPort.saveMasterPrompt(findDomain(id).activate())
        val agent = agentDataPort.findAgentById(active.agentId)
        if (agent != null && agent.masterPromptId != active.id) {
            agentDataPort.saveAgent(agent.copy(masterPromptId = active.id))
        }
        return agentMapper.toResponse(active)
    }

    fun archive(id: UUID): MasterPromptResponse =
        agentMapper.toResponse(agentDataPort.saveMasterPrompt(findDomain(id).archive()))

    private fun findDomain(id: UUID): MasterPrompt =
        agentDataPort.findMasterPromptById(id) ?: throw NotFoundException("Мастер-промпт не найден")
}
