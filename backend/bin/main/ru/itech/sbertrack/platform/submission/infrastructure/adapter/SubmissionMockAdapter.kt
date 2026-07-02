package ru.itech.sbertrack.platform.submission.infrastructure.adapter

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore
import ru.itech.sbertrack.platform.submission.domain.model.Submission
import ru.itech.sbertrack.platform.submission.domain.port.SubmissionDataPort
import java.util.UUID

@Component
class SubmissionMockAdapter(
    private val dataStore: MockPlatformDataStore,
) : SubmissionDataPort {
    override fun list(): List<Submission> =
        dataStore.submissions.values.sortedByDescending { it.submittedAt ?: java.time.Instant.EPOCH }

    override fun findById(id: UUID): Submission? =
        dataStore.submissions[id]

    override fun save(submission: Submission): Submission {
        dataStore.submissions[submission.id] = submission
        return submission
    }
}
