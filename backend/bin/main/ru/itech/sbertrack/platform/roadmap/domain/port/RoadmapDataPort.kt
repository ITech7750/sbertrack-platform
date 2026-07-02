package ru.itech.sbertrack.platform.roadmap.domain.port

import ru.itech.sbertrack.platform.roadmap.domain.model.StudentRoadmap
import java.util.UUID

interface RoadmapDataPort {
    fun findById(id: UUID): StudentRoadmap?
    fun findByStudentId(studentId: UUID): StudentRoadmap?
    fun save(roadmap: StudentRoadmap): StudentRoadmap
}
