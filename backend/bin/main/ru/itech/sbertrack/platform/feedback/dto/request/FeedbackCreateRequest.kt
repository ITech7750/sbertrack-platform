package ru.itech.sbertrack.platform.feedback.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.feedback.domain.model.FeedbackAuthorType
import ru.itech.sbertrack.platform.submission.domain.model.SubmissionStatus
import java.util.UUID

@Schema(description = "Создание feedback")
data class FeedbackCreateRequest(
    val submissionId: UUID,
    val authorType: FeedbackAuthorType,
    @field:NotBlank
    val authorName: String,
    @field:NotBlank
    val text: String,
    val recommendations: List<String> = emptyList(),
    val competencyDelta: Map<Competency, Int> = emptyMap(),
    val targetSubmissionStatus: SubmissionStatus? = null,
)
