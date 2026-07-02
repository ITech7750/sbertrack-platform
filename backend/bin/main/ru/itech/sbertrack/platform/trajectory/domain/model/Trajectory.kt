package ru.itech.sbertrack.platform.trajectory.domain.model

import ru.itech.sbertrack.platform.common.model.Difficulty
import java.time.Instant
import java.util.UUID

data class Trajectory(
    val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String,
    val targetRoleTitle: String,
    val targetRoleDescription: String,
    val direction: String,
    val difficulty: Difficulty,
    val estimatedDurationWeeks: Int,
    val nodeIds: List<UUID>,
    val createdAt: Instant = Instant.now(),
) {
    init {
        require(title.isNotBlank()) { "Название траектории обязательно" }
        require(targetRoleTitle.isNotBlank()) { "Целевой маяк профессии обязателен" }
        require(estimatedDurationWeeks > 0) { "Длительность должна быть положительной" }
    }
}
