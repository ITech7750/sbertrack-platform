package ru.itech.sbertrack.platform.trajectory.application.mapper

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.trajectory.domain.model.Trajectory
import ru.itech.sbertrack.platform.trajectory.domain.model.TrajectoryNode
import ru.itech.sbertrack.platform.trajectory.dto.response.TrajectoryNodeResponse
import ru.itech.sbertrack.platform.trajectory.dto.response.TrajectoryResponse

@Component
class TrajectoryMapper {
    fun toResponse(trajectory: Trajectory): TrajectoryResponse =
        TrajectoryResponse(
            id = trajectory.id,
            title = trajectory.title,
            description = trajectory.description,
            targetRoleTitle = trajectory.targetRoleTitle,
            targetRoleDescription = trajectory.targetRoleDescription,
            direction = trajectory.direction,
            difficulty = trajectory.difficulty,
            estimatedDurationWeeks = trajectory.estimatedDurationWeeks,
            nodeIds = trajectory.nodeIds,
            createdAt = trajectory.createdAt,
        )

    fun toResponse(node: TrajectoryNode): TrajectoryNodeResponse =
        TrajectoryNodeResponse(
            id = node.id,
            trajectoryId = node.trajectoryId,
            title = node.title,
            description = node.description,
            type = node.type,
            positionX = node.positionX,
            positionY = node.positionY,
            trackId = node.trackId,
            caseId = node.caseId,
            requiredCompetencies = node.requiredCompetencies,
            status = node.status,
            nextNodeIds = node.nextNodeIds,
        )
}
