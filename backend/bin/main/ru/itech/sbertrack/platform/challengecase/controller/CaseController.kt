package ru.itech.sbertrack.platform.challengecase.controller

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
import ru.itech.sbertrack.platform.challengecase.application.service.CaseService
import ru.itech.sbertrack.platform.challengecase.domain.model.CaseStatus
import ru.itech.sbertrack.platform.challengecase.dto.request.CaseCreateRequest
import ru.itech.sbertrack.platform.challengecase.dto.request.CaseUpdateRequest
import ru.itech.sbertrack.platform.challengecase.dto.response.CaseResponse
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.common.model.Difficulty
import java.util.UUID

@Tag(name = "Cases")
@RestController
@RequestMapping("/api/v1/cases")
class CaseController(
    private val caseService: CaseService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) trackId: UUID?,
        @RequestParam(required = false) status: CaseStatus?,
        @RequestParam(required = false) difficulty: Difficulty?,
        @RequestParam(required = false) tag: String?,
        @RequestParam(required = false) competency: Competency?,
    ): List<CaseResponse> =
        caseService.list(trackId, status, difficulty, tag, competency)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): CaseResponse =
        caseService.get(id)

    @PostMapping
    fun create(
        @Valid @RequestBody request: CaseCreateRequest,
        @RequestHeader("Authorization", required = false) authorization: String?,
    ): CaseResponse =
        caseService.create(request, authorization)

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: CaseUpdateRequest): CaseResponse =
        caseService.update(id, request)

    @PostMapping("/{id}/send-to-moderation")
    fun sendToModeration(@PathVariable id: UUID): CaseResponse =
        caseService.sendToModeration(id)

    @PostMapping("/{id}/publish")
    fun publish(@PathVariable id: UUID): CaseResponse =
        caseService.publish(id)

    @PostMapping("/{id}/archive")
    fun archive(@PathVariable id: UUID): CaseResponse =
        caseService.archive(id)
}
