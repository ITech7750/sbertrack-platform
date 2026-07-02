package ru.itech.sbertrack.platform.cvbook.infrastructure.adapter

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore
import ru.itech.sbertrack.platform.cvbook.domain.model.CvBookCandidate
import ru.itech.sbertrack.platform.cvbook.domain.port.CvBookDataPort
import java.util.UUID

@Component
class CvBookMockAdapter(
    private val dataStore: MockPlatformDataStore,
) : CvBookDataPort {
    override fun list(): List<CvBookCandidate> =
        dataStore.cvBookCandidates.values.sortedWith(compareByDescending<CvBookCandidate> { it.priorityStatus.name }.thenByDescending { it.averageScore })

    override fun findById(id: UUID): CvBookCandidate? =
        dataStore.cvBookCandidates[id]

    override fun findByStudentId(studentId: UUID): CvBookCandidate? =
        dataStore.cvBookCandidates.values.firstOrNull { it.studentId == studentId }

    override fun save(candidate: CvBookCandidate): CvBookCandidate {
        dataStore.cvBookCandidates[candidate.id] = candidate
        return candidate
    }
}
