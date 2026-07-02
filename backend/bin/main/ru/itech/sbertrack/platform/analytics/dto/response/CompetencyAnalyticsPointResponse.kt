package ru.itech.sbertrack.platform.analytics.dto.response

import ru.itech.sbertrack.platform.common.model.Competency

data class CompetencyAnalyticsPointResponse(
    val competency: Competency,
    val value: Int,
    val previousValue: Int? = null,
)
