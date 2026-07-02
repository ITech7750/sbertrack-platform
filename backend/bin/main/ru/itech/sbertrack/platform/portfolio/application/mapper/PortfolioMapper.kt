package ru.itech.sbertrack.platform.portfolio.application.mapper

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.portfolio.domain.model.Portfolio
import ru.itech.sbertrack.platform.portfolio.dto.response.PortfolioResponse

@Component
class PortfolioMapper {
    fun toResponse(portfolio: Portfolio): PortfolioResponse =
        PortfolioResponse(
            id = portfolio.id,
            studentId = portfolio.studentId,
            summary = portfolio.summary,
            completedCases = portfolio.completedCases,
            competencyProfile = portfolio.competencyProfile,
            artifacts = portfolio.artifacts,
            feedbackHighlights = portfolio.feedbackHighlights,
            cvBookIncluded = portfolio.cvBookIncluded,
        )
}
