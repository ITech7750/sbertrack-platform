package ru.itech.sbertrack.platform.track.domain.model

import ru.itech.sbertrack.platform.common.model.Difficulty
import java.time.Instant
import java.util.UUID

data class Track(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String,
    val customerId: UUID,
    val customerName: String,
    val difficulty: Difficulty,
    val status: TrackStatus = TrackStatus.DRAFT,
    val targetAudience: String,
    val caseIds: List<UUID> = emptyList(),
    val createdAt: Instant = Instant.now(),
) {
    init {
        require(title.isNotBlank()) { "Название трека обязательно" }
        require(description.isNotBlank()) { "Описание трека обязательно" }
        require(targetAudience.isNotBlank()) { "Целевая аудитория обязательна" }
    }

    fun update(
        title: String,
        description: String,
        difficulty: Difficulty,
        status: TrackStatus,
        targetAudience: String,
    ): Track = copy(
        title = title,
        description = description,
        difficulty = difficulty,
        status = status,
        targetAudience = targetAudience,
    )

    fun addCase(caseId: UUID): Track =
        if (caseId in caseIds) this else copy(caseIds = caseIds + caseId)
}
