package ru.itech.sbertrack.platform.track.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.common.model.Difficulty
import ru.itech.sbertrack.platform.track.application.service.TrackService
import ru.itech.sbertrack.platform.track.dto.request.TrackCreateRequest
import ru.itech.sbertrack.platform.track.dto.request.TrackUpdateRequest
import ru.itech.sbertrack.platform.track.dto.response.TrackResponse
import java.util.UUID

@Tag(name = "Tracks")
@RestController
@RequestMapping("/api/v1/tracks")
class TrackController(
    private val trackService: TrackService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) difficulty: Difficulty?,
        @RequestParam(required = false) targetAudience: String?,
        @RequestParam(required = false) competency: Competency?,
        @RequestParam(required = false) customerName: String?,
    ): List<TrackResponse> =
        trackService.list(difficulty, targetAudience, competency, customerName)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): TrackResponse =
        trackService.get(id)

    @PostMapping
    fun create(
        @Valid @RequestBody request: TrackCreateRequest,
        @RequestHeader("Authorization", required = false) authorization: String?,
    ): TrackResponse =
        trackService.create(request, authorization)

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: TrackUpdateRequest): TrackResponse =
        trackService.update(id, request)
}
