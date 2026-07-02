package ru.itech.sbertrack.platform.challengecase.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import ru.itech.sbertrack.platform.challengecase.domain.model.FeedbackMode
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.common.model.Difficulty
import java.time.LocalDate

@Schema(description = "Обновление кейса")
data class CaseUpdateRequest(
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val shortDescription: String,
    @field:NotBlank
    val fullDescription: String,
    val difficulty: Difficulty,
    @field:Min(1)
    val participantLimit: Int,
    @field:NotBlank
    val expectedResult: String,
    val feedbackMode: FeedbackMode,
    val competencyWeights: Map<Competency, Int>,
    val tags: List<String> = emptyList(),
    val deadline: LocalDate,
)
