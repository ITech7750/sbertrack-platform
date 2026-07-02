package ru.itech.sbertrack.platform.challengecase.application.mapper

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.challengecase.domain.model.PracticalCase
import ru.itech.sbertrack.platform.challengecase.dto.response.CaseResponse

@Component
class CaseMapper {
    fun toResponse(practicalCase: PracticalCase): CaseResponse =
        CaseResponse(
            id = practicalCase.id,
            trackId = practicalCase.trackId,
            title = practicalCase.title,
            shortDescription = practicalCase.shortDescription,
            fullDescription = practicalCase.fullDescription,
            customerId = practicalCase.customerId,
            customerName = practicalCase.customerName,
            status = practicalCase.status,
            difficulty = practicalCase.difficulty,
            participantLimit = practicalCase.participantLimit,
            expectedResult = practicalCase.expectedResult,
            feedbackMode = practicalCase.feedbackMode,
            competencyWeights = practicalCase.competencyWeights,
            tags = practicalCase.tags,
            deadline = practicalCase.deadline,
            createdAt = practicalCase.createdAt,
        )
}
