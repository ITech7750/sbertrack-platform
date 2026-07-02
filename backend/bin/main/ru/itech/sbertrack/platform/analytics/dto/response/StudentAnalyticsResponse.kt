package ru.itech.sbertrack.platform.analytics.dto.response

data class StudentAnalyticsResponse(
    val greeting: String,
    val trajectoryTitle: String,
    val currentLevel: String,
    val goal: String,
    val nearestCaseTitle: String,
    val roadmapProgress: Int,
    val metrics: List<MetricResponse>,
    val completedCasesByDirection: List<ChartPointResponse>,
    val competencyRadar: List<CompetencyAnalyticsPointResponse>,
    val competencyGrowth: List<ChartPointResponse>,
    val qualityDynamics: List<ChartPointResponse>,
    val funnel: List<ChartPointResponse>,
    val recommendation: String,
)
