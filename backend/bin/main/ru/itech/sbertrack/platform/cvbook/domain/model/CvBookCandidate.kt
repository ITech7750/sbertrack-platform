package ru.itech.sbertrack.platform.cvbook.domain.model

import ru.itech.sbertrack.platform.common.model.Competency
import java.time.Instant
import java.util.UUID

data class CvBookCandidate(
    val id: UUID = UUID.randomUUID(),
    val studentId: UUID,
    val fullName: String,
    val organizationName: String?,
    val completedCasesCount: Int,
    val averageScore: Int,
    val competencyProfile: Map<Competency, Int>,
    val tags: List<String>,
    val priorityStatus: PriorityStatus,
    val lastActivityAt: Instant = Instant.now(),
) {
    fun markPriority(): CvBookCandidate = copy(priorityStatus = PriorityStatus.PRIORITY, lastActivityAt = Instant.now())
}
