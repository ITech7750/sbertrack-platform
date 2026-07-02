package ru.itech.sbertrack.platform.roadmap.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.roadmap.application.service.RoadmapService
import ru.itech.sbertrack.platform.roadmap.dto.response.StudentRoadmapResponse
import java.util.UUID

@Tag(name = "Roadmaps")
@RestController
@RequestMapping("/api/v1/roadmaps")
class RoadmapController(
    private val roadmapService: RoadmapService,
) {
    @GetMapping("/me")
    fun me(@RequestHeader("Authorization", required = false) authorization: String?): StudentRoadmapResponse =
        roadmapService.current(authorization)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): StudentRoadmapResponse =
        roadmapService.get(id)

    @PostMapping("/{id}/steps/{stepId}/start")
    fun start(@PathVariable id: UUID, @PathVariable stepId: UUID): StudentRoadmapResponse =
        roadmapService.startStep(id, stepId)

    @PostMapping("/{id}/steps/{stepId}/complete")
    fun complete(@PathVariable id: UUID, @PathVariable stepId: UUID): StudentRoadmapResponse =
        roadmapService.completeStep(id, stepId)
}
