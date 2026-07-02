package ru.itech.sbertrack.platform.trajectory.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.roadmap.dto.response.StudentRoadmapResponse
import ru.itech.sbertrack.platform.trajectory.application.service.TrajectoryService
import ru.itech.sbertrack.platform.trajectory.dto.response.TrajectoryNodeResponse
import ru.itech.sbertrack.platform.trajectory.dto.response.TrajectoryResponse
import java.util.UUID

@Tag(name = "Trajectories")
@RestController
@RequestMapping("/api/v1/trajectories")
class TrajectoryController(
    private val trajectoryService: TrajectoryService,
) {
    @GetMapping
    fun list(): List<TrajectoryResponse> = trajectoryService.list()

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): TrajectoryResponse = trajectoryService.get(id)

    @GetMapping("/{id}/nodes")
    fun nodes(@PathVariable id: UUID): List<TrajectoryNodeResponse> = trajectoryService.nodes(id)

    @PostMapping("/{id}/select")
    fun select(
        @PathVariable id: UUID,
        @RequestHeader("Authorization", required = false) authorization: String?,
    ): StudentRoadmapResponse = trajectoryService.select(id, authorization)
}
