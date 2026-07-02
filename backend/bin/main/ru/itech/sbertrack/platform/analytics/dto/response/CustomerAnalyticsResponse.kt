package ru.itech.sbertrack.platform.analytics.dto.response

data class CustomerAnalyticsResponse(
    val metrics: List<MetricResponse>,
    val submissionStatuses: List<ChartPointResponse>,
    val participantsByTrack: List<ChartPointResponse>,
    val averageCompetencies: List<CompetencyAnalyticsPointResponse>,
    val activityDynamics: List<ChartPointResponse>,
    val funnel: List<ChartPointResponse>,
)
