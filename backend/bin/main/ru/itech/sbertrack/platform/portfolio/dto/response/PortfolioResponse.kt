package ru.itech.sbertrack.platform.portfolio.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.common.model.Competency
import java.util.UUID

@Schema(description = "Портфолио студента")
data class PortfolioResponse(
    val id: UUID,
    val studentId: UUID,
    val summary: String,
    val completedCases: List<String>,
    val competencyProfile: Map<Competency, Int>,
    val artifacts: List<String>,
    val feedbackHighlights: List<String>,
    val cvBookIncluded: Boolean,
)
