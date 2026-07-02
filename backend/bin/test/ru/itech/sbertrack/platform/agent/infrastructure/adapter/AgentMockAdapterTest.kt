package ru.itech.sbertrack.platform.agent.infrastructure.adapter

import kotlin.test.Test
import kotlin.test.assertContains
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore

class AgentMockAdapterTest {
    @Test
    fun `mock agent response keeps mentor boundaries`() {
        val dataStore = MockPlatformDataStore()
        val adapter = AgentMockAdapter(dataStore)
        val agent = adapter.listAgents().first { it.code == "backend-architect" }

        val response = adapter.generateAssistantResponse(agent, emptyList(), "Помоги спроектировать backend")

        assertContains(response, "Я помогу тебе мыслить, проверять гипотезы и структурировать работу")
        assertContains(response, "Важно: итоговое решение должен оформить сам участник")
        assertContains(response, "контракты")
    }
}
