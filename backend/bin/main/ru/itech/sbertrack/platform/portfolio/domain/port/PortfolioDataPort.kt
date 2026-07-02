package ru.itech.sbertrack.platform.portfolio.domain.port

import ru.itech.sbertrack.platform.portfolio.domain.model.Portfolio
import java.util.UUID

interface PortfolioDataPort {
    fun list(): List<Portfolio>
    fun findByStudentId(studentId: UUID): Portfolio?
    fun save(portfolio: Portfolio): Portfolio
}
