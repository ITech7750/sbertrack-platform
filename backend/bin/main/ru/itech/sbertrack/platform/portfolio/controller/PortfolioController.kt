package ru.itech.sbertrack.platform.portfolio.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.portfolio.application.service.PortfolioService
import ru.itech.sbertrack.platform.portfolio.dto.response.PortfolioResponse
import java.util.UUID

@Tag(name = "Portfolio")
@RestController
@RequestMapping("/api/v1/portfolio")
class PortfolioController(
    private val portfolioService: PortfolioService,
) {
    @GetMapping("/me")
    fun me(@RequestHeader("Authorization", required = false) authorization: String?): PortfolioResponse =
        portfolioService.current(authorization)

    @GetMapping("/{studentId}")
    fun get(@PathVariable studentId: UUID): PortfolioResponse =
        portfolioService.get(studentId)
}
