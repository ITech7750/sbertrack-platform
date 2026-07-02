package ru.itech.sbertrack.platform.roadmap.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.roadmap.application.mapper.RoadmapMapper
import ru.itech.sbertrack.platform.roadmap.domain.model.RoadmapStep
import ru.itech.sbertrack.platform.roadmap.domain.model.StudentRoadmap
import ru.itech.sbertrack.platform.roadmap.domain.port.RoadmapDataPort
import ru.itech.sbertrack.platform.roadmap.dto.response.StudentRoadmapResponse
import ru.itech.sbertrack.platform.trajectory.domain.model.TrajectoryNodeStatus
import ru.itech.sbertrack.platform.trajectory.domain.port.TrajectoryDataPort
import java.time.LocalDate
import java.util.UUID

@Service
class RoadmapService(
    private val roadmapDataPort: RoadmapDataPort,
    private val trajectoryDataPort: TrajectoryDataPort,
    private val roadmapMapper: RoadmapMapper,
    private val authService: AuthService,
) {
    fun current(authorization: String?): StudentRoadmapResponse {
        val user = authService.currentUser(authorization)
        return roadmapDataPort.findByStudentId(user.id)?.let(roadmapMapper::toResponse)
            ?: selectTrajectory(user.id, trajectoryDataPort.list().first().id)
    }

    fun get(id: UUID): StudentRoadmapResponse =
        roadmapDataPort.findById(id)?.let(roadmapMapper::toResponse)
            ?: throw NotFoundException("Дорожная карта не найдена")

    fun selectTrajectory(studentId: UUID, trajectoryId: UUID): StudentRoadmapResponse {
        val trajectory = trajectoryDataPort.findById(trajectoryId) ?: throw NotFoundException("Траектория не найдена")
        val nodes = trajectoryDataPort.listNodes(trajectoryId)
        val roadmapId = UUID.randomUUID()
        val steps = nodes.mapIndexed { index, node ->
            RoadmapStep(
                roadmapId = roadmapId,
                nodeId = node.id,
                title = node.title,
                description = node.description,
                status = when (index) {
                    0 -> TrajectoryNodeStatus.COMPLETED
                    1 -> TrajectoryNodeStatus.IN_PROGRESS
                    2 -> TrajectoryNodeStatus.AVAILABLE
                    else -> TrajectoryNodeStatus.LOCKED
                },
                caseId = node.caseId,
                orderIndex = index,
            )
        }
        val currentStep = steps.firstOrNull { it.status == TrajectoryNodeStatus.IN_PROGRESS } ?: steps.first()
        val roadmap = StudentRoadmap(
            id = roadmapId,
            studentId = studentId,
            trajectoryId = trajectory.id,
            title = "Дорожная карта: ${trajectory.title}",
            currentNodeId = currentStep.nodeId,
            progressPercent = 18,
            expectedFinishDate = LocalDate.now().plusWeeks(trajectory.estimatedDurationWeeks.toLong()),
            steps = steps,
        )
        return roadmapMapper.toResponse(roadmapDataPort.save(roadmap))
    }

    fun startStep(roadmapId: UUID, stepId: UUID): StudentRoadmapResponse {
        val roadmap = roadmapDataPort.findById(roadmapId) ?: throw NotFoundException("Дорожная карта не найдена")
        val step = roadmap.steps.firstOrNull { it.id == stepId } ?: throw NotFoundException("Этап не найден")
        return roadmapMapper.toResponse(roadmapDataPort.save(roadmap.withStep(step.start())))
    }

    fun completeStep(roadmapId: UUID, stepId: UUID): StudentRoadmapResponse {
        val roadmap = roadmapDataPort.findById(roadmapId) ?: throw NotFoundException("Дорожная карта не найдена")
        val step = roadmap.steps.firstOrNull { it.id == stepId } ?: throw NotFoundException("Этап не найден")
        return roadmapMapper.toResponse(roadmapDataPort.save(roadmap.withStep(step.complete())))
    }
}
