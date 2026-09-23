package ru.itech.sbertrack.platform.submission.infrastructure.persistence

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.submission.domain.model.Submission
import ru.itech.sbertrack.platform.submission.domain.port.SubmissionDataPort
import java.util.UUID

@Component
class SubmissionJpaAdapter(
    private val repository: SubmissionJpaRepository,
    private val mapper: SubmissionEntityMapper,
) : SubmissionDataPort {
    override fun findForAgentContext(studentId: UUID, caseId: UUID): List<Submission> =
        repository.findTop5ByStudentIdAndCaseIdOrderByContentUpdatedAtDescIdAsc(studentId, caseId).map(mapper::toDomain)

    override fun list(): List<Submission> =
        repository.findAll().map(mapper::toDomain)

    override fun findById(id: UUID): Submission? =
        repository.findById(id).orElse(null)?.let(mapper::toDomain)

    override fun save(submission: Submission): Submission {
        repository.save(mapper.toEntity(submission))
        return submission
    }
}
