package ru.itech.sbertrack.platform.trajectory.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.roadmap.application.service.RoadmapService
import ru.itech.sbertrack.platform.roadmap.dto.response.StudentRoadmapResponse
import ru.itech.sbertrack.platform.trajectory.application.mapper.TrajectoryMapper
import ru.itech.sbertrack.platform.trajectory.domain.port.TrajectoryDataPort
import ru.itech.sbertrack.platform.trajectory.dto.response.TrajectoryNodeResponse
import ru.itech.sbertrack.platform.trajectory.dto.response.TrajectoryResponse
import java.util.UUID

@Service
class TrajectoryService(
    private val trajectoryDataPort: TrajectoryDataPort,
    private val trajectoryMapper: TrajectoryMapper,
    private val roadmapService: RoadmapService,
    private val authService: AuthService,
) {
    fun list(): List<TrajectoryResponse> =
        trajectoryDataPort.list().map(trajectoryMapper::toResponse)

    fun get(id: UUID): TrajectoryResponse =
        trajectoryDataPort.findById(id)?.let(trajectoryMapper::toResponse)
            ?: throw NotFoundException("Траектория не найдена")

    fun nodes(id: UUID): List<TrajectoryNodeResponse> {
        trajectoryDataPort.findById(id) ?: throw NotFoundException("Траектория не найдена")
        return trajectoryDataPort.listNodes(id).map(trajectoryMapper::toResponse)
    }

    fun select(id: UUID, authorization: String?): StudentRoadmapResponse {
        val user = authService.currentUser(authorization)
        return roadmapService.selectTrajectory(user.id, id)
    }
}
