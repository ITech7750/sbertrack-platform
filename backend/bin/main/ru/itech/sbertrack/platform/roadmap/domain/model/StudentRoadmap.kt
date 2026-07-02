package ru.itech.sbertrack.platform.roadmap.domain.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class StudentRoadmap(
    val id: UUID = UUID.randomUUID(),
    val studentId: UUID,
    val trajectoryId: UUID,
    val title: String,
    val currentNodeId: UUID,
    val progressPercent: Int,
    val selectedAt: Instant = Instant.now(),
    val expectedFinishDate: LocalDate,
    val steps: List<RoadmapStep>,
) {
    fun withStep(updatedStep: RoadmapStep): StudentRoadmap {
        val updatedSteps = steps.map { if (it.id == updatedStep.id) updatedStep else it }
            .unlockNextAfter(updatedStep)
        val current = updatedSteps.firstOrNull { it.status.name == "IN_PROGRESS" }
            ?: updatedSteps.firstOrNull { it.status.name == "AVAILABLE" }
            ?: updatedSteps.last()
        val completed = updatedSteps.count { it.status.name == "COMPLETED" }
        return copy(
            currentNodeId = current.nodeId,
            progressPercent = ((completed.toDouble() / updatedSteps.size) * 100).toInt().coerceIn(0, 100),
            steps = updatedSteps,
        )
    }

    private fun List<RoadmapStep>.unlockNextAfter(updatedStep: RoadmapStep): List<RoadmapStep> {
        if (updatedStep.status.name != "COMPLETED") return this
        val nextIndex = updatedStep.orderIndex + 1
        return map { step ->
            if (step.orderIndex == nextIndex && step.status.name == "LOCKED") {
                step.copy(status = ru.itech.sbertrack.platform.trajectory.domain.model.TrajectoryNodeStatus.AVAILABLE)
            } else {
                step
            }
        }
    }
}
