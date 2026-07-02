package ru.itech.sbertrack.platform.portfolio.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.portfolio.application.mapper.PortfolioMapper
import ru.itech.sbertrack.platform.portfolio.dto.response.PortfolioResponse
import ru.itech.sbertrack.platform.portfolio.domain.port.PortfolioDataPort
import java.util.UUID

@Service
class PortfolioService(
    private val portfolioDataPort: PortfolioDataPort,
    private val portfolioMapper: PortfolioMapper,
    private val authService: AuthService,
) {
    fun current(authorization: String?): PortfolioResponse {
        val user = authService.currentUser(authorization)
        return get(user.id)
    }

    fun get(studentId: UUID): PortfolioResponse =
        portfolioDataPort.findByStudentId(studentId)?.let(portfolioMapper::toResponse)
            ?: throw NotFoundException("Портфолио не найдено")
}
