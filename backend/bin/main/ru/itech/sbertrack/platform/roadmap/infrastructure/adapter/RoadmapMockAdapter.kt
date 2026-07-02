package ru.itech.sbertrack.platform.roadmap.infrastructure.adapter

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore
import ru.itech.sbertrack.platform.roadmap.domain.model.StudentRoadmap
import ru.itech.sbertrack.platform.roadmap.domain.port.RoadmapDataPort
import java.util.UUID

@Component
class RoadmapMockAdapter(
    private val dataStore: MockPlatformDataStore,
) : RoadmapDataPort {
    override fun findById(id: UUID): StudentRoadmap? =
        dataStore.roadmaps[id]

    override fun findByStudentId(studentId: UUID): StudentRoadmap? =
        dataStore.roadmaps.values.firstOrNull { it.studentId == studentId }

    override fun save(roadmap: StudentRoadmap): StudentRoadmap {
        dataStore.roadmaps.values.removeIf { it.studentId == roadmap.studentId && it.id != roadmap.id }
        dataStore.roadmaps[roadmap.id] = roadmap
        return roadmap
    }
}
