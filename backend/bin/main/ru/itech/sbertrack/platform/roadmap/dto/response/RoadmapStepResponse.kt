package ru.itech.sbertrack.platform.roadmap.dto.response

import ru.itech.sbertrack.platform.trajectory.domain.model.TrajectoryNodeStatus
import java.util.UUID

data class RoadmapStepResponse(
    val id: UUID,
    val roadmapId: UUID,
    val nodeId: UUID,
    val title: String,
    val description: String,
    val status: TrajectoryNodeStatus,
    val caseId: UUID?,
    val orderIndex: Int,
)
