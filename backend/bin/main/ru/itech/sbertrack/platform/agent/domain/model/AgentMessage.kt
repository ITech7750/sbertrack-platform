package ru.itech.sbertrack.platform.agent.domain.model

import java.time.Instant
import java.util.UUID

data class AgentMessage(
    val id: UUID = UUID.randomUUID(),
    val sessionId: UUID,
    val role: AgentMessageRole,
    val content: String,
    val createdAt: Instant = Instant.now(),
) {
    init {
        require(content.isNotBlank()) { "Сообщение агенту не может быть пустым" }
    }
}
