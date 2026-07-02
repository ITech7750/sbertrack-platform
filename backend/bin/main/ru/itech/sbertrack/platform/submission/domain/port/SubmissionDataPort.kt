package ru.itech.sbertrack.platform.submission.domain.port

import ru.itech.sbertrack.platform.submission.domain.model.Submission
import java.util.UUID

interface SubmissionDataPort {
    fun list(): List<Submission>
    fun findById(id: UUID): Submission?
    fun save(submission: Submission): Submission
}
