package ru.itech.sbertrack.platform.portfolio.infrastructure.adapter

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore
import ru.itech.sbertrack.platform.portfolio.domain.model.Portfolio
import ru.itech.sbertrack.platform.portfolio.domain.port.PortfolioDataPort
import java.util.UUID

@Component
class PortfolioMockAdapter(
    private val dataStore: MockPlatformDataStore,
) : PortfolioDataPort {
    override fun list(): List<Portfolio> =
        dataStore.portfolios.values.sortedBy { it.summary }

    override fun findByStudentId(studentId: UUID): Portfolio? =
        dataStore.portfolios[studentId]

    override fun save(portfolio: Portfolio): Portfolio {
        dataStore.portfolios[portfolio.studentId] = portfolio
        return portfolio
    }
}
