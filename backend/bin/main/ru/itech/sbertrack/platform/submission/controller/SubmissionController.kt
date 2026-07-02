package ru.itech.sbertrack.platform.submission.controller

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
import ru.itech.sbertrack.platform.feedback.application.service.FeedbackService
import ru.itech.sbertrack.platform.feedback.dto.request.FeedbackCreateRequest
import ru.itech.sbertrack.platform.feedback.dto.response.FeedbackResponse
import ru.itech.sbertrack.platform.submission.application.service.SubmissionService
import ru.itech.sbertrack.platform.submission.domain.model.SubmissionStatus
import ru.itech.sbertrack.platform.submission.dto.request.SubmissionCreateRequest
import ru.itech.sbertrack.platform.submission.dto.request.SubmissionUpdateRequest
import ru.itech.sbertrack.platform.submission.dto.response.SubmissionResponse
import java.util.UUID

@Tag(name = "Submissions")
@RestController
@RequestMapping("/api/v1/submissions")
class SubmissionController(
    private val submissionService: SubmissionService,
    private val feedbackService: FeedbackService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) caseId: UUID?,
        @RequestParam(required = false) studentId: UUID?,
        @RequestParam(required = false) status: SubmissionStatus?,
    ): List<SubmissionResponse> =
        submissionService.list(caseId, studentId, status)

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): SubmissionResponse =
        submissionService.get(id)

    @PostMapping
    fun create(
        @Valid @RequestBody request: SubmissionCreateRequest,
        @RequestHeader("Authorization", required = false) authorization: String?,
    ): SubmissionResponse =
        submissionService.create(request, authorization)

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: SubmissionUpdateRequest): SubmissionResponse =
        submissionService.update(id, request)

    @PostMapping("/{id}/submit")
    fun submit(@PathVariable id: UUID): SubmissionResponse =
        submissionService.submit(id)

    @PostMapping("/{id}/feedback")
    fun feedback(@PathVariable id: UUID, @Valid @RequestBody request: FeedbackCreateRequest): FeedbackResponse =
        feedbackService.create(request.copy(submissionId = id))

    @PostMapping("/{id}/mark-priority")
    fun markPriority(@PathVariable id: UUID): SubmissionResponse =
        submissionService.markPriority(id)
}
