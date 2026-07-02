package ru.itech.sbertrack.platform.feedback.infrastructure.adapter

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore
import ru.itech.sbertrack.platform.feedback.domain.model.Feedback
import ru.itech.sbertrack.platform.feedback.domain.port.FeedbackDataPort
import java.util.UUID

@Component
class FeedbackMockAdapter(
    private val dataStore: MockPlatformDataStore,
) : FeedbackDataPort {
    override fun list(): List<Feedback> =
        dataStore.feedback.values.sortedByDescending { it.createdAt }

    override fun findById(id: UUID): Feedback? =
        dataStore.feedback[id]

    override fun listBySubmissionId(submissionId: UUID): List<Feedback> =
        dataStore.feedback.values.filter { it.submissionId == submissionId }.sortedByDescending { it.createdAt }

    override fun save(feedback: Feedback): Feedback {
        dataStore.feedback[feedback.id] = feedback
        return feedback
    }
}
