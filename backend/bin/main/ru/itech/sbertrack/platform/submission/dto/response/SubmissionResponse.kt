package ru.itech.sbertrack.platform.submission.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.submission.domain.model.SubmissionStatus
import java.time.Instant
import java.util.UUID

@Schema(description = "Отправка решения")
data class SubmissionResponse(
    val id: UUID,
    val caseId: UUID,
    val studentId: UUID,
    val teamName: String?,
    val title: String,
    val description: String,
    val artifactUrl: String?,
    val status: SubmissionStatus,
    val competencyScores: Map<Competency, Int>,
    val feedbackIds: List<UUID>,
    val reflectionId: UUID?,
    val submittedAt: Instant?,
)
