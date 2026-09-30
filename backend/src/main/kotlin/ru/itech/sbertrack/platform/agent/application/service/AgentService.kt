package ru.itech.sbertrack.platform.agent.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.agent.application.mapper.AgentMapper
import ru.itech.sbertrack.platform.agent.domain.model.*
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.agent.domain.port.AgentGatewayPort
import ru.itech.sbertrack.platform.agent.dto.request.AgentMessageRequest
import ru.itech.sbertrack.platform.agent.dto.request.AgentSessionCreateRequest
import ru.itech.sbertrack.platform.agent.dto.response.AgentDefinitionResponse
import ru.itech.sbertrack.platform.agent.dto.response.AgentSessionResponse
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.common.exception.ForbiddenException
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.user.domain.model.UserRole
import ru.itech.sbertrack.platform.user.domain.model.UserStatus
import java.util.UUID

@Service
class AgentService(
    private val agentDataPort: AgentDataPort,
    private val agentGatewayPort: AgentGatewayPort,
    private val agentMapper: AgentMapper,
    private val authService: AuthService,
    private val requestBuilder: AgentRequestBuilder,
) {
    fun listAgents(): List<AgentDefinitionResponse> = agentDataPort.listAgents().map(agentMapper::toResponse)

    fun getAgent(id: UUID): AgentDefinitionResponse =
        agentDataPort.findAgentById(id)?.let(agentMapper::toResponse) ?: throw NotFoundException("Агент не найден")

    fun createSession(request: AgentSessionCreateRequest, authorization: String?): AgentSessionResponse {
        val studentId = currentStudent(authorization)
        if (request.studentId != null && request.studentId != studentId) throw ForbiddenException("Нельзя создать чужую сессию")
        val agent = activeAgent(request.agentId)
        request.caseId?.let(requestBuilder::accessibleCase)
        return agentMapper.toResponse(agentDataPort.saveSession(AgentSession(
            studentId = studentId, caseId = request.caseId, agentId = agent.id,
        )))
    }

    fun getSession(id: UUID, authorization: String?): AgentSessionResponse =
        agentMapper.toResponse(ownedSession(id, authorization))

    fun findLatestSession(agentId: UUID, caseId: UUID?, authorization: String?): AgentSessionResponse? {
        val studentId = currentStudent(authorization)
        activeAgent(agentId)
        return agentDataPort.findLatestSession(studentId, agentId, caseId)?.let(agentMapper::toResponse)
    }

    fun sendMessage(sessionId: UUID, request: AgentMessageRequest, authorization: String?): AgentSessionResponse {
        val session = ownedSession(sessionId, authorization)
        val agent = activeAgent(session.agentId)
        val assistantText = agentGatewayPort.generateAssistantResponse(requestBuilder.build(agent, session, request))
        // Commit a complete exchange only if the history used for generation is still current.
        return agentMapper.toResponse(agentDataPort.appendExchange(session.id, session.messages.size,
            AgentMessage(sessionId = session.id, role = AgentMessageRole.USER, content = request.content),
            AgentMessage(sessionId = session.id, role = AgentMessageRole.AGENT, content = assistantText),
        ))
    }

    private fun ownedSession(id: UUID, authorization: String?): AgentSession {
        val studentId = currentStudent(authorization)
        val session = agentDataPort.findSessionById(id) ?: throw NotFoundException("Сессия агента не найдена")
        if (session.studentId != studentId) throw ForbiddenException("Нет доступа к сессии")
        return session
    }

    private fun currentStudent(authorization: String?): UUID {
        val user = authService.currentUser(authorization)
        if (user.role != UserRole.STUDENT || user.status != UserStatus.ACTIVE) throw ForbiddenException("Чат доступен активным студентам")
        return user.id
    }

    private fun activeAgent(id: UUID): AgentDefinition {
        val agent = agentDataPort.findAgentById(id) ?: throw NotFoundException("Агент не найден")
        if (agent.status != AgentStatus.ACTIVE) throw ForbiddenException("Агент недоступен")
        return agent
    }
}
