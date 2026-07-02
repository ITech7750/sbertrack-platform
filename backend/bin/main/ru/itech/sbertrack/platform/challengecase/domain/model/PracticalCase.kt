package ru.itech.sbertrack.platform.challengecase.domain.model

import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.common.model.Difficulty
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class PracticalCase(
    val id: UUID = UUID.randomUUID(),
    val trackId: UUID,
    val title: String,
    val shortDescription: String,
    val fullDescription: String,
    val customerId: UUID,
    val customerName: String,
    val status: CaseStatus = CaseStatus.DRAFT,
    val difficulty: Difficulty,
    val participantLimit: Int,
    val expectedResult: String,
    val feedbackMode: FeedbackMode,
    val competencyWeights: Map<Competency, Int>,
    val tags: List<String>,
    val deadline: LocalDate,
    val createdAt: Instant = Instant.now(),
) {
    init {
        require(title.isNotBlank()) { "Название кейса обязательно" }
        require(shortDescription.isNotBlank()) { "Краткое описание обязательно" }
        require(fullDescription.isNotBlank()) { "Полное описание обязательно" }
        require(participantLimit > 0) { "Лимит участников должен быть положительным" }
        require(expectedResult.isNotBlank()) { "Ожидаемый результат обязателен" }
        require(competencyWeights.isNotEmpty()) { "Нужно указать вклад в компетенции" }
        require(competencyWeights.values.all { it in 0..100 }) { "Веса компетенций должны быть от 0 до 100" }
    }

    fun update(
        title: String,
        shortDescription: String,
        fullDescription: String,
        difficulty: Difficulty,
        participantLimit: Int,
        expectedResult: String,
        feedbackMode: FeedbackMode,
        competencyWeights: Map<Competency, Int>,
        tags: List<String>,
        deadline: LocalDate,
    ): PracticalCase = copy(
        title = title,
        shortDescription = shortDescription,
        fullDescription = fullDescription,
        difficulty = difficulty,
        participantLimit = participantLimit,
        expectedResult = expectedResult,
        feedbackMode = feedbackMode,
        competencyWeights = competencyWeights,
        tags = tags,
        deadline = deadline,
    )

    fun sendToModeration(): PracticalCase {
        require(status == CaseStatus.DRAFT) { "На модерацию можно отправить только черновик" }
        return copy(status = CaseStatus.MODERATION)
    }

    fun publish(): PracticalCase {
        require(status == CaseStatus.MODERATION || status == CaseStatus.DRAFT) { "Опубликовать можно черновик или кейс на модерации" }
        return copy(status = CaseStatus.PUBLISHED)
    }

    fun archive(): PracticalCase = copy(status = CaseStatus.ARCHIVED)

    fun returnToDraft(): PracticalCase = copy(status = CaseStatus.DRAFT)
}
