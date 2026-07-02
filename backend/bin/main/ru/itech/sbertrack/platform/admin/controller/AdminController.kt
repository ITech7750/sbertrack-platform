package ru.itech.sbertrack.platform.admin.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.admin.application.service.AdminService
import ru.itech.sbertrack.platform.admin.dto.response.AdminDashboardResponse
import ru.itech.sbertrack.platform.admin.dto.response.AdminStatisticsResponse

@Tag(name = "Admin")
@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminService: AdminService,
) {
    @GetMapping("/dashboard")
    fun dashboard(): AdminDashboardResponse =
        adminService.dashboard()

    @GetMapping("/statistics")
    fun statistics(): AdminStatisticsResponse =
        adminService.statistics()
}
