package ru.itech.sbertrack.platform.trajectory.dto.response

import ru.itech.sbertrack.platform.common.model.Difficulty
import java.time.Instant
import java.util.UUID

data class TrajectoryResponse(
    val id: UUID,
    val title: String,
    val description: String,
    val targetRoleTitle: String,
    val targetRoleDescription: String,
    val direction: String,
    val difficulty: Difficulty,
    val estimatedDurationWeeks: Int,
    val nodeIds: List<UUID>,
    val createdAt: Instant,
)
