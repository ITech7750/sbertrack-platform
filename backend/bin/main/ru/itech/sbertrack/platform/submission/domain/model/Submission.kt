package ru.itech.sbertrack.platform.submission.domain.model

import ru.itech.sbertrack.platform.common.model.Competency
import java.time.Instant
import java.util.UUID

data class Submission(
    val id: UUID = UUID.randomUUID(),
    val caseId: UUID,
    val studentId: UUID,
    val teamName: String? = null,
    val title: String,
    val description: String,
    val artifactUrl: String? = null,
    val status: SubmissionStatus = SubmissionStatus.DRAFT,
    val competencyScores: Map<Competency, Int> = emptyMap(),
    val feedbackIds: List<UUID> = emptyList(),
    val reflectionId: UUID? = null,
    val submittedAt: Instant? = null,
) {
    init {
        require(title.isNotBlank()) { "Название решения обязательно" }
    }

    fun updateDraft(title: String, description: String, artifactUrl: String?, teamName: String?): Submission {
        require(status == SubmissionStatus.DRAFT || status == SubmissionStatus.NEEDS_IMPROVEMENT) {
            "Редактировать можно черновик или отправку на доработке"
        }
        return copy(title = title, description = description, artifactUrl = artifactUrl, teamName = teamName)
    }

    fun submit(scores: Map<Competency, Int>): Submission =
        copy(
            status = SubmissionStatus.SUBMITTED,
            competencyScores = scores,
            submittedAt = Instant.now(),
        )

    fun addFeedback(feedbackId: UUID, status: SubmissionStatus = this.status): Submission =
        copy(feedbackIds = feedbackIds + feedbackId, status = status)

    fun attachReflection(reflectionId: UUID): Submission = copy(reflectionId = reflectionId)

    fun markPriority(): Submission = copy(status = SubmissionStatus.PRIORITY_CANDIDATE)
}
