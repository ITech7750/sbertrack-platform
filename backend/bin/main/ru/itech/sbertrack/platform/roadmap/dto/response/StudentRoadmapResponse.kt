package ru.itech.sbertrack.platform.roadmap.dto.response

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class StudentRoadmapResponse(
    val id: UUID,
    val studentId: UUID,
    val trajectoryId: UUID,
    val title: String,
    val currentNodeId: UUID,
    val progressPercent: Int,
    val selectedAt: Instant,
    val expectedFinishDate: LocalDate,
    val steps: List<RoadmapStepResponse>,
)
