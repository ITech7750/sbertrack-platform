package ru.itech.sbertrack.platform.agent.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.agent.application.mapper.AgentMapper
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessage
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessageRole
import ru.itech.sbertrack.platform.agent.domain.model.AgentSession
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.agent.domain.port.AgentGatewayPort
import ru.itech.sbertrack.platform.agent.dto.request.AgentMessageRequest
import ru.itech.sbertrack.platform.agent.dto.request.AgentSessionCreateRequest
import ru.itech.sbertrack.platform.agent.dto.response.AgentDefinitionResponse
import ru.itech.sbertrack.platform.agent.dto.response.AgentSessionResponse
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.user.domain.model.UserRole
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort
import java.util.UUID

@Service
class AgentService(
    private val agentDataPort: AgentDataPort,
    private val agentGatewayPort: AgentGatewayPort,
    private val userDataPort: UserDataPort,
    private val agentMapper: AgentMapper,
    private val authService: AuthService,
) {
    fun listAgents(): List<AgentDefinitionResponse> =
        agentDataPort.listAgents().map(agentMapper::toResponse)

    fun getAgent(id: UUID): AgentDefinitionResponse =
        agentDataPort.findAgentById(id)?.let(agentMapper::toResponse)
            ?: throw NotFoundException("Агент не найден")

    fun createSession(request: AgentSessionCreateRequest, authorization: String?): AgentSessionResponse {
        val agent = agentDataPort.findAgentById(request.agentId) ?: throw NotFoundException("Агент не найден")
        val studentId = request.studentId
            ?: authService.tryCurrentUser(authorization)?.id
            ?: userDataPort.list().first { it.role == UserRole.STUDENT }.id
        val session = AgentSession(
            studentId = studentId,
            caseId = request.caseId,
            agentId = agent.id,
        )
        return agentMapper.toResponse(agentDataPort.saveSession(session))
    }

    fun getSession(id: UUID): AgentSessionResponse =
        agentDataPort.findSessionById(id)?.let(agentMapper::toResponse)
            ?: throw NotFoundException("Сессия агента не найдена")

    fun sendMessage(sessionId: UUID, request: AgentMessageRequest): AgentSessionResponse {
        val session = agentDataPort.findSessionById(sessionId) ?: throw NotFoundException("Сессия агента не найдена")
        val agent = agentDataPort.findAgentById(session.agentId) ?: throw NotFoundException("Агент не найден")
        val userMessage = AgentMessage(
            sessionId = session.id,
            role = AgentMessageRole.USER,
            content = request.content,
        )
        val withUserMessage = session.addMessage(userMessage)
        val assistantText = agentGatewayPort.generateAssistantResponse(
            agent,
            withUserMessage.messages,
            request.content,
        )
        val assistantMessage = AgentMessage(
            sessionId = session.id,
            role = AgentMessageRole.AGENT,
            content = assistantText,
        )
        return agentMapper.toResponse(agentDataPort.saveSession(withUserMessage.addMessage(assistantMessage)))
    }

    private fun buildContextText(request: AgentMessageRequest): String {
        val caseLine = request.caseTitle?.takeIf { it.isNotBlank() }?.let { "Контекст наставника: кейс \"$it\".\n" } ?: ""
        val artifactsLine = request.artifacts.takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "Наработки участника: ", postfix = ".\n") ?: ""
        return caseLine + artifactsLine
    }
}
