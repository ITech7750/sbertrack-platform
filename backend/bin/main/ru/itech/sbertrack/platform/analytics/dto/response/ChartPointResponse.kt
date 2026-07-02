package ru.itech.sbertrack.platform.analytics.dto.response

data class ChartPointResponse(
    val label: String,
    val value: Int,
    val secondaryValue: Int? = null,
)
