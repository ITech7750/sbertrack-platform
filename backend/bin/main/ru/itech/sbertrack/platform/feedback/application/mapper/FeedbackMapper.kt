package ru.itech.sbertrack.platform.feedback.application.mapper

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.feedback.domain.model.Feedback
import ru.itech.sbertrack.platform.feedback.dto.response.FeedbackResponse

@Component
class FeedbackMapper {
    fun toResponse(feedback: Feedback): FeedbackResponse =
        FeedbackResponse(
            id = feedback.id,
            submissionId = feedback.submissionId,
            authorType = feedback.authorType,
            authorName = feedback.authorName,
            text = feedback.text,
            recommendations = feedback.recommendations,
            competencyDelta = feedback.competencyDelta,
            createdAt = feedback.createdAt,
        )
}
