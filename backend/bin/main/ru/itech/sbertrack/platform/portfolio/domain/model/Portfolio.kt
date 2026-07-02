package ru.itech.sbertrack.platform.portfolio.domain.model

import ru.itech.sbertrack.platform.common.model.Competency
import java.util.UUID

data class Portfolio(
    val id: UUID = UUID.randomUUID(),
    val studentId: UUID,
    val summary: String,
    val completedCases: List<String>,
    val competencyProfile: Map<Competency, Int>,
    val artifacts: List<String>,
    val feedbackHighlights: List<String>,
    val cvBookIncluded: Boolean,
) {
    fun includeCompletedCase(caseTitle: String, artifactUrl: String?, scores: Map<Competency, Int>): Portfolio {
        val mergedProfile = Competency.entries.associateWith { competency ->
            val current = competencyProfile[competency] ?: 0
            val delta = scores[competency] ?: 0
            (current + delta / 4).coerceAtMost(100)
        }
        return copy(
            completedCases = (completedCases + caseTitle).distinct(),
            competencyProfile = mergedProfile,
            artifacts = if (artifactUrl.isNullOrBlank()) artifacts else (artifacts + artifactUrl).distinct(),
        )
    }

    fun addFeedbackHighlight(text: String): Portfolio =
        copy(feedbackHighlights = (listOf(text) + feedbackHighlights).take(6))
}
