package ru.itech.sbertrack.platform.trajectory.domain.model

import ru.itech.sbertrack.platform.common.model.Competency
import java.util.UUID

data class TrajectoryNode(
    val id: UUID = UUID.randomUUID(),
    val trajectoryId: UUID,
    val title: String,
    val description: String,
    val type: TrajectoryNodeType,
    val positionX: Int,
    val positionY: Int,
    val trackId: UUID?,
    val caseId: UUID?,
    val requiredCompetencies: Map<Competency, Int>,
    val status: TrajectoryNodeStatus,
    val nextNodeIds: List<UUID>,
)
