package ru.itech.sbertrack.platform.trajectory.domain.port

import ru.itech.sbertrack.platform.trajectory.domain.model.Trajectory
import ru.itech.sbertrack.platform.trajectory.domain.model.TrajectoryNode
import java.util.UUID

interface TrajectoryDataPort {
    fun list(): List<Trajectory>
    fun findById(id: UUID): Trajectory?
    fun listNodes(trajectoryId: UUID): List<TrajectoryNode>
    fun findNodeById(id: UUID): TrajectoryNode?
}
