package ru.itech.sbertrack.platform.analytics.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.analytics.application.service.AnalyticsService
import ru.itech.sbertrack.platform.analytics.dto.response.AdminAnalyticsResponse
import ru.itech.sbertrack.platform.analytics.dto.response.CompetencyAnalyticsPointResponse
import ru.itech.sbertrack.platform.analytics.dto.response.CustomerAnalyticsResponse
import ru.itech.sbertrack.platform.analytics.dto.response.ModeratorAnalyticsResponse
import ru.itech.sbertrack.platform.analytics.dto.response.StudentAnalyticsResponse

@Tag(name = "Analytics")
@RestController
@RequestMapping("/api/v1/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService,
) {
    @GetMapping("/student/dashboard")
    fun studentDashboard(@RequestHeader("Authorization", required = false) authorization: String?): StudentAnalyticsResponse =
        analyticsService.studentDashboard(authorization)

    @GetMapping("/student/competencies")
    fun studentCompetencies(@RequestHeader("Authorization", required = false) authorization: String?): List<CompetencyAnalyticsPointResponse> =
        analyticsService.studentCompetencies(authorization)

    @GetMapping("/customer/dashboard")
    fun customerDashboard(): CustomerAnalyticsResponse =
        analyticsService.customerDashboard()

    @GetMapping("/customer/candidates")
    fun customerCandidates(): CustomerAnalyticsResponse =
        analyticsService.candidateAnalytics()

    @GetMapping("/moderator/dashboard")
    fun moderatorDashboard(): ModeratorAnalyticsResponse =
        analyticsService.moderatorDashboard()

    @GetMapping("/admin/dashboard")
    fun adminDashboard(): AdminAnalyticsResponse =
        analyticsService.adminDashboard()
}
