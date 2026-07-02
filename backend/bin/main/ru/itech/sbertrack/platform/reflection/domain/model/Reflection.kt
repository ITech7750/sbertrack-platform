package ru.itech.sbertrack.platform.reflection.domain.model

import java.time.Instant
import java.util.UUID

data class Reflection(
    val id: UUID = UUID.randomUUID(),
    val submissionId: UUID,
    val studentId: UUID,
    val answers: Map<String, String>,
    val summary: String,
    val createdAt: Instant = Instant.now(),
) {
    init {
        require(answers.isNotEmpty()) { "Нужно заполнить ответы рефлексии" }
        require(summary.isNotBlank()) { "Summary рефлексии обязательно" }
    }
}
