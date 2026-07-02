package ru.itech.sbertrack.platform.agent.domain.model

import java.time.Instant
import java.util.UUID

data class AgentSession(
    val id: UUID = UUID.randomUUID(),
    val studentId: UUID,
    val caseId: UUID?,
    val agentId: UUID,
    val messages: List<AgentMessage> = emptyList(),
    val createdAt: Instant = Instant.now(),
) {
    fun addMessage(message: AgentMessage): AgentSession = copy(messages = messages + message)
}
