package ru.itech.sbertrack.platform.cvbook.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.cvbook.domain.model.PriorityStatus
import java.time.Instant
import java.util.UUID

@Schema(description = "Кандидат CV-book")
data class CvBookCandidateResponse(
    val id: UUID,
    val studentId: UUID,
    val fullName: String,
    val organizationName: String?,
    val completedCasesCount: Int,
    val averageScore: Int,
    val competencyProfile: Map<Competency, Int>,
    val tags: List<String>,
    val priorityStatus: PriorityStatus,
    val lastActivityAt: Instant,
)
