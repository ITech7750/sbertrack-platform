package ru.itech.sbertrack.platform.roadmap.domain.model

import ru.itech.sbertrack.platform.trajectory.domain.model.TrajectoryNodeStatus
import java.util.UUID

data class RoadmapStep(
    val id: UUID = UUID.randomUUID(),
    val roadmapId: UUID,
    val nodeId: UUID,
    val title: String,
    val description: String,
    val status: TrajectoryNodeStatus,
    val caseId: UUID?,
    val orderIndex: Int,
) {
    fun start(): RoadmapStep {
        require(status == TrajectoryNodeStatus.AVAILABLE) { "Начать можно только доступный этап" }
        return copy(status = TrajectoryNodeStatus.IN_PROGRESS)
    }

    fun complete(): RoadmapStep {
        require(status == TrajectoryNodeStatus.IN_PROGRESS || status == TrajectoryNodeStatus.AVAILABLE) {
            "Завершить можно доступный этап или этап в работе"
        }
        return copy(status = TrajectoryNodeStatus.COMPLETED)
    }
}
