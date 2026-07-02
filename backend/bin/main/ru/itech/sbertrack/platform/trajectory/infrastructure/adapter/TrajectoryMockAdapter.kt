package ru.itech.sbertrack.platform.trajectory.infrastructure.adapter

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore
import ru.itech.sbertrack.platform.trajectory.domain.model.Trajectory
import ru.itech.sbertrack.platform.trajectory.domain.model.TrajectoryNode
import ru.itech.sbertrack.platform.trajectory.domain.port.TrajectoryDataPort
import java.util.UUID

@Component
class TrajectoryMockAdapter(
    private val dataStore: MockPlatformDataStore,
) : TrajectoryDataPort {
    override fun list(): List<Trajectory> =
        dataStore.trajectories.values.sortedBy { it.title }

    override fun findById(id: UUID): Trajectory? =
        dataStore.trajectories[id]

    override fun listNodes(trajectoryId: UUID): List<TrajectoryNode> =
        dataStore.trajectoryNodes.values
            .filter { it.trajectoryId == trajectoryId }
            .sortedWith(compareBy<TrajectoryNode> { it.positionY }.thenBy { it.positionX })

    override fun findNodeById(id: UUID): TrajectoryNode? =
        dataStore.trajectoryNodes[id]
}
