package ru.itech.sbertrack.platform.feedback.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.feedback.application.service.FeedbackService
import ru.itech.sbertrack.platform.feedback.dto.request.FeedbackCreateRequest
import ru.itech.sbertrack.platform.feedback.dto.response.FeedbackResponse
import java.util.UUID

@Tag(name = "Feedback")
@RestController
@RequestMapping("/api/v1/feedback")
class FeedbackController(
    private val feedbackService: FeedbackService,
) {
    @GetMapping("/submission/{submissionId}")
    fun bySubmission(@PathVariable submissionId: UUID): List<FeedbackResponse> =
        feedbackService.listBySubmission(submissionId)

    @PostMapping
    fun create(@Valid @RequestBody request: FeedbackCreateRequest): FeedbackResponse =
        feedbackService.create(request)
}
