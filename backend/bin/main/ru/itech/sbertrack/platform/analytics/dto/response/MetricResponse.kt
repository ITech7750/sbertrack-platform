package ru.itech.sbertrack.platform.analytics.dto.response

data class MetricResponse(
    val label: String,
    val value: Int,
    val unit: String? = null,
    val trend: Int? = null,
)
