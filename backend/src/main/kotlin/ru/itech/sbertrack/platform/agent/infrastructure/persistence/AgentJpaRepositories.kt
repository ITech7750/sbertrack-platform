package ru.itech.sbertrack.platform.agent.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import jakarta.persistence.LockModeType
import java.util.UUID

interface AgentJpaRepository : JpaRepository<AgentEntity, UUID>

interface MasterPromptJpaRepository : JpaRepository<MasterPromptEntity, UUID>

interface AgentSessionJpaRepository : JpaRepository<AgentSessionEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AgentSessionEntity s where s.id = :id")
    fun findForAppend(@Param("id") id: UUID): AgentSessionEntity?

    fun findFirstByStudentIdAndAgentIdAndCaseIdOrderByCreatedAtDesc(
        studentId: UUID,
        agentId: UUID,
        caseId: UUID,
    ): AgentSessionEntity?

    fun findFirstByStudentIdAndAgentIdAndCaseIdIsNullOrderByCreatedAtDesc(
        studentId: UUID,
        agentId: UUID,
    ): AgentSessionEntity?
}

interface AgentMessageJpaRepository : JpaRepository<AgentMessageEntity, UUID> {
    fun findBySessionIdOrderBySequenceNumber(sessionId: UUID): List<AgentMessageEntity>
}
