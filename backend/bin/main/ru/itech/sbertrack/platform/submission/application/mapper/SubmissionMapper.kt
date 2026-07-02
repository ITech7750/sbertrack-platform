package ru.itech.sbertrack.platform.submission.application.mapper

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.submission.domain.model.Submission
import ru.itech.sbertrack.platform.submission.dto.response.SubmissionResponse

@Component
class SubmissionMapper {
    fun toResponse(submission: Submission): SubmissionResponse =
        SubmissionResponse(
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
        )
}
