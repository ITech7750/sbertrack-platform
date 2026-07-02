package ru.itech.sbertrack.platform.agent.infrastructure.adapter

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.agent.domain.model.AgentDefinition
import ru.itech.sbertrack.platform.agent.domain.model.AgentMessage
import ru.itech.sbertrack.platform.agent.domain.model.AgentSession
import ru.itech.sbertrack.platform.agent.domain.model.AgentSpecialization
import ru.itech.sbertrack.platform.agent.domain.model.MasterPrompt
import ru.itech.sbertrack.platform.agent.domain.port.AgentDataPort
import ru.itech.sbertrack.platform.agent.domain.port.AgentGatewayPort
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore
import java.util.UUID

@Component
class AgentMockAdapter(
    private val dataStore: MockPlatformDataStore,
) : AgentDataPort, AgentGatewayPort {
    override fun listAgents(): List<AgentDefinition> =
        dataStore.agents.values.sortedBy { it.name }

    override fun findAgentById(id: UUID): AgentDefinition? =
        dataStore.agents[id]

    override fun saveAgent(agentDefinition: AgentDefinition): AgentDefinition {
        dataStore.agents[agentDefinition.id] = agentDefinition
        return agentDefinition
    }

    override fun listMasterPrompts(): List<MasterPrompt> =
        dataStore.masterPrompts.values.sortedByDescending { it.updatedAt }

    override fun findMasterPromptById(id: UUID): MasterPrompt? =
        dataStore.masterPrompts[id]

    override fun saveMasterPrompt(masterPrompt: MasterPrompt): MasterPrompt {
        dataStore.masterPrompts[masterPrompt.id] = masterPrompt
        return masterPrompt
    }

    override fun findSessionById(id: UUID): AgentSession? =
        dataStore.agentSessions[id]

    override fun saveSession(agentSession: AgentSession): AgentSession {
        dataStore.agentSessions[agentSession.id] = agentSession
        return agentSession
    }

    override fun generateAssistantResponse(
        agentDefinition: AgentDefinition,
        history: List<AgentMessage>,
        userMessage: String,
    ): String {
        val focus = when (agentDefinition.specialization) {
            AgentSpecialization.BACKEND_ARCHITECTURE -> "доменные модули, REST-контракты, роли, данные и границы интеграций"
            AgentSpecialization.FRONTEND_INTERFACE -> "структуру экрана, сценарии пользователя, Material Design, формы, таблицы и дашборды"
            AgentSpecialization.PRODUCT_HYPOTHESIS -> "сегмент пользователя, проверяемую гипотезу, метрики и план эксперимента"
            AgentSpecialization.FINANCIAL_MODELING -> "драйверы модели, сценарии, чувствительность и проверку исходных допущений"
            AgentSpecialization.MARKET_RESEARCH -> "источники, критерии сравнения, сегменты рынка и проверяемые выводы"
            AgentSpecialization.REFLECTION -> "личный вклад, сложные места, использование ИИ и следующую итерацию"
            AgentSpecialization.FEEDBACK -> "сильные стороны решения, зоны роста и конкретные рекомендации"
        }
        val normalized = userMessage.trim().replace(Regex("\\s+"), " ")
        return """
            Я помогу тебе мыслить, проверять гипотезы и структурировать работу, но итоговое решение остаётся твоим.

            Как можно подойти к задаче:
            Разложи запрос "$normalized" на $focus. Сначала зафиксируй цель, затем ограничения и критерии готовности.

            Что проверить самостоятельно:
            Какие предположения ты сделал? Что является твоим собственным выводом? Где нужны факты, а не ощущение уверенности?

            Какие артефакты подготовить:
            Краткое описание решения, схему или таблицу решений, список рисков, критерии проверки и вывод по следующему шагу.

            Следующий шаг:
            Выбери один участок работы, оформи черновик и проверь его по вопросам выше.

            Важно: итоговое решение должен оформить сам участник.
        """.trimIndent()
    }
}
