package ru.itech.sbertrack.platform.submission.infrastructure.persistence

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.submission.domain.model.Submission

@Component
class SubmissionEntityMapper {
    fun toDomain(entity: SubmissionEntity): Submission =
        Submission(
            id = entity.id,
            caseId = entity.caseId,
            studentId = entity.studentId,
            teamName = entity.teamName,
            title = entity.title,
            description = entity.description,
            artifactUrl = entity.artifactUrl,
            status = entity.status,
            competencyScores = entity.competencyScores,
            feedbackIds = entity.feedbackIds,
            reflectionId = entity.reflectionId,
            submittedAt = entity.submittedAt,
            contentUpdatedAt = entity.contentUpdatedAt,
        )

    fun toEntity(submission: Submission): SubmissionEntity =
        SubmissionEntity(
            id = submission.id,
            caseId = submission.caseId,
            studentId = submission.studentId,
            teamName = submission.teamName,
            title = submission.title,
            description = submission.description,
            artifactUrl = submission.artifactUrl,
            status = submission.status,
            competencyScores = submission.competencyScores,
            feedbackIds = submission.feedbackIds,
            reflectionId = submission.reflectionId,
            submittedAt = submission.submittedAt,
            contentUpdatedAt = submission.contentUpdatedAt,
        )
}
