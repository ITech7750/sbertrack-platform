package ru.itech.sbertrack.platform.agent.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.agent.application.service.AgentService
import ru.itech.sbertrack.platform.agent.dto.request.AgentMessageRequest
import ru.itech.sbertrack.platform.agent.dto.request.AgentSessionCreateRequest
import ru.itech.sbertrack.platform.agent.dto.response.AgentDefinitionResponse
import ru.itech.sbertrack.platform.agent.dto.response.AgentSessionResponse
import java.util.UUID

@Tag(name = "Agents")
@RestController
@RequestMapping("/api/v1/agents")
class AgentController(
    private val agentService: AgentService,
) {
    @GetMapping
    fun listAgents(): List<AgentDefinitionResponse> =
        agentService.listAgents()

    @GetMapping("/{id}")
    fun getAgent(@PathVariable id: UUID): AgentDefinitionResponse =
        agentService.getAgent(id)

    @PostMapping("/sessions")
    fun createSession(
        @Valid @RequestBody request: AgentSessionCreateRequest,
        @RequestHeader("Authorization", required = false) authorization: String?,
    ): AgentSessionResponse =
        agentService.createSession(request, authorization)

    @GetMapping("/sessions/{id}")
    fun getSession(@PathVariable id: UUID): AgentSessionResponse =
        agentService.getSession(id)

    @PostMapping("/sessions/{id}/messages")
    fun sendMessage(@PathVariable id: UUID, @Valid @RequestBody request: AgentMessageRequest): AgentSessionResponse =
        agentService.sendMessage(id, request)
}
