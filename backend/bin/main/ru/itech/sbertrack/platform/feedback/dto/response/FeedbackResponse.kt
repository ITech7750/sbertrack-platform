package ru.itech.sbertrack.platform.feedback.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.feedback.domain.model.FeedbackAuthorType
import java.time.Instant
import java.util.UUID

@Schema(description = "Feedback по решению")
data class FeedbackResponse(
    val id: UUID,
    val submissionId: UUID,
    val authorType: FeedbackAuthorType,
    val authorName: String,
    val text: String,
    val recommendations: List<String>,
    val competencyDelta: Map<Competency, Int>,
    val createdAt: Instant,
)
