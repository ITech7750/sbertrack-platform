package ru.itech.sbertrack.platform.analytics.dto.response

data class AdminAnalyticsResponse(
    val metrics: List<MetricResponse>,
    val usersByRole: List<ChartPointResponse>,
    val casesByStatus: List<ChartPointResponse>,
    val activityDynamics: List<ChartPointResponse>,
    val platformCompetencies: List<CompetencyAnalyticsPointResponse>,
    val participationFunnel: List<ChartPointResponse>,
)
