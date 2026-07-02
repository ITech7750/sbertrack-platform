package ru.itech.sbertrack.platform.analytics.dto.response

data class ModeratorAnalyticsResponse(
    val metrics: List<MetricResponse>,
    val agentUsage: List<ChartPointResponse>,
    val agentUsefulness: List<ChartPointResponse>,
    val recentSessions: List<String>,
)
