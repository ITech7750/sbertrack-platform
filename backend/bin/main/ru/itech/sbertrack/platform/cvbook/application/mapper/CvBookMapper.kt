package ru.itech.sbertrack.platform.cvbook.application.mapper

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.cvbook.domain.model.CvBookCandidate
import ru.itech.sbertrack.platform.cvbook.dto.response.CvBookCandidateResponse

@Component
class CvBookMapper {
    fun toResponse(candidate: CvBookCandidate): CvBookCandidateResponse =
        CvBookCandidateResponse(
            id = candidate.id,
            studentId = candidate.studentId,
            fullName = candidate.fullName,
            organizationName = candidate.organizationName,
            completedCasesCount = candidate.completedCasesCount,
            averageScore = candidate.averageScore,
            competencyProfile = candidate.competencyProfile,
            tags = candidate.tags,
            priorityStatus = candidate.priorityStatus,
            lastActivityAt = candidate.lastActivityAt,
        )
}
