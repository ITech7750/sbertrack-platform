package ru.itech.sbertrack.platform.feedback.domain.model

import ru.itech.sbertrack.platform.common.model.Competency
import java.time.Instant
import java.util.UUID

data class Feedback(
    val id: UUID = UUID.randomUUID(),
    val submissionId: UUID,
    val authorType: FeedbackAuthorType,
    val authorName: String,
    val text: String,
    val recommendations: List<String>,
    val competencyDelta: Map<Competency, Int>,
    val createdAt: Instant = Instant.now(),
) {
    init {
        require(authorName.isNotBlank()) { "Автор feedback обязателен" }
        require(text.isNotBlank()) { "Текст feedback обязателен" }
    }
}
