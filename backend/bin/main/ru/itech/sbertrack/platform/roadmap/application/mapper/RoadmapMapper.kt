package ru.itech.sbertrack.platform.roadmap.application.mapper

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.roadmap.domain.model.RoadmapStep
import ru.itech.sbertrack.platform.roadmap.domain.model.StudentRoadmap
import ru.itech.sbertrack.platform.roadmap.dto.response.RoadmapStepResponse
import ru.itech.sbertrack.platform.roadmap.dto.response.StudentRoadmapResponse

@Component
class RoadmapMapper {
    fun toResponse(roadmap: StudentRoadmap): StudentRoadmapResponse =
        StudentRoadmapResponse(
            id = roadmap.id,
            studentId = roadmap.studentId,
            trajectoryId = roadmap.trajectoryId,
            title = roadmap.title,
            currentNodeId = roadmap.currentNodeId,
            progressPercent = roadmap.progressPercent,
            selectedAt = roadmap.selectedAt,
            expectedFinishDate = roadmap.expectedFinishDate,
            steps = roadmap.steps.map(::toResponse),
        )

    private fun toResponse(step: RoadmapStep): RoadmapStepResponse =
        RoadmapStepResponse(
            id = step.id,
            roadmapId = step.roadmapId,
            nodeId = step.nodeId,
            title = step.title,
            description = step.description,
            status = step.status,
            caseId = step.caseId,
            orderIndex = step.orderIndex,
        )
}
