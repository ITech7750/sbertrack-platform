package ru.itech.sbertrack.platform.agent

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import ru.itech.sbertrack.platform.agent.domain.model.*
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.agent.infrastructure.persistence.AgentMessageEntity
import ru.itech.sbertrack.platform.agent.infrastructure.persistence.AgentMessageJpaRepository
import ru.itech.sbertrack.platform.common.exception.ApiException
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.*

@SpringBootTest
class AgentMessageOrderingIntegrationTest {
    @Autowired lateinit var agents: AgentDataPort
    @Autowired lateinit var users: UserDataPort
    @Autowired lateinit var messages: AgentMessageJpaRepository
    @Autowired lateinit var jdbc: JdbcTemplate
    private val sessionIds = mutableListOf<UUID>()

    private fun session(): AgentSession = agents.saveSession(AgentSession(
        studentId = users.findByEmail("student@example.com")!!.id,
        caseId = null, agentId = agents.listAgents().first().id,
    )).also { sessionIds.add(it.id) }

    @AfterEach fun cleanUp() {
        sessionIds.forEach {
            jdbc.update("delete from agent_messages where session_id = ?", it)
            jdbc.update("delete from agent_sessions where id = ?", it)
        }
    }

    @Test fun `read order follows sequence even with equal timestamps and reversed inserts`() {
        val session = session()
        val time = Instant.parse("2026-01-01T00:00:00Z")
        fun row(sequence: Long, role: AgentMessageRole, content: String, createdAt: Instant = time) =
            AgentMessageEntity(UUID.randomUUID(), session.id, role, content, createdAt, sequence)
        messages.saveAllAndFlush(listOf(
            row(2, AgentMessageRole.AGENT, "answer"), row(1, AgentMessageRole.USER, "question"),
            row(4, AgentMessageRole.AGENT, "next answer", time.minusSeconds(1)),
            row(3, AgentMessageRole.USER, "next question", time.minusSeconds(1)),
        ))
        val expected = listOf("question", "answer", "next question", "next answer")
        assertEquals(expected, agents.findSessionById(session.id)!!.messages.map { it.content })
        assertEquals(expected, agents.findLatestSession(session.studentId, session.agentId, null)!!.messages.map { it.content })
    }

    @Test fun `concurrent appends commit one complete pair and reject stale generation`() {
        val session = session()
        val barrier = CyclicBarrier(2)
        val executor = Executors.newFixedThreadPool(2)
        val time = Instant.parse("2026-01-01T00:00:00Z")
        try {
            val calls = (1..2).map { n -> executor.submit<Result<AgentSession>> {
                barrier.await(10, TimeUnit.SECONDS)
                runCatching { agents.appendExchange(session.id, 0,
                    AgentMessage(sessionId = session.id, role = AgentMessageRole.USER, content = "question-$n", createdAt = time),
                    AgentMessage(sessionId = session.id, role = AgentMessageRole.AGENT, content = "answer-$n", createdAt = time),
                ) }
            } }
            val results = calls.map { it.get(15, TimeUnit.SECONDS) }
            assertEquals(1, results.count { it.isSuccess })
            assertEquals(HttpStatus.CONFLICT, (results.single { it.isFailure }.exceptionOrNull() as ApiException).httpStatus)
            val history = agents.findSessionById(session.id)!!.messages
            val winner = history.first().content.substringAfter('-')
            assertEquals(listOf("question-$winner", "answer-$winner"), history.map { it.content })
            assertEquals(listOf(1L, 2L), messages.findBySessionIdOrderBySequenceNumber(session.id).map { it.sequenceNumber })

            val retried = agents.appendExchange(session.id, history.size,
                AgentMessage(sessionId = session.id, role = AgentMessageRole.USER, content = "retry", createdAt = time),
                AgentMessage(sessionId = session.id, role = AgentMessageRole.AGENT, content = "retry answer", createdAt = time),
            )
            assertEquals(listOf("question-$winner", "answer-$winner", "retry", "retry answer"), retried.messages.map { it.content })
            assertEquals(listOf(1L, 2L, 3L, 4L), messages.findBySessionIdOrderBySequenceNumber(session.id).map { it.sequenceNumber })
        } finally { executor.shutdownNow() }
    }
}
