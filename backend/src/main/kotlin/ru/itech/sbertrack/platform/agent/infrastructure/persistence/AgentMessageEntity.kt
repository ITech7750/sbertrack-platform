package ru.itech.sbertrack.platform.agent.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessageRole
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "agent_messages")
class AgentMessageEntity(
    @Id
    val id: UUID,
    @Column(name = "session_id", nullable = false)
    val sessionId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    val role: AgentMessageRole,
    @Column(nullable = false)
    val content: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant,
    @Column(name = "sequence_number", nullable = false)
    val sequenceNumber: Long,
)
