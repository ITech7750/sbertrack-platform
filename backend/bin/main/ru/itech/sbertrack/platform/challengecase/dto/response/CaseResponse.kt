package ru.itech.sbertrack.platform.challengecase.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.challengecase.domain.model.CaseStatus
import ru.itech.sbertrack.platform.challengecase.domain.model.FeedbackMode
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.common.model.Difficulty
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Schema(description = "Кейс")
data class CaseResponse(
    val id: UUID,
    val trackId: UUID,
    val title: String,
    val shortDescription: String,
    val fullDescription: String,
    val customerId: UUID,
    val customerName: String,
    val status: CaseStatus,
    val difficulty: Difficulty,
    val participantLimit: Int,
    val expectedResult: String,
    val feedbackMode: FeedbackMode,
    val competencyWeights: Map<Competency, Int>,
    val tags: List<String>,
    val deadline: LocalDate,
    val createdAt: Instant,
)
