package ru.itech.sbertrack.platform.feedback.domain.port

import ru.itech.sbertrack.platform.feedback.domain.model.Feedback
import java.util.UUID

interface FeedbackDataPort {
    fun list(): List<Feedback>
    fun findById(id: UUID): Feedback?
    fun listBySubmissionId(submissionId: UUID): List<Feedback>
    fun save(feedback: Feedback): Feedback
}
