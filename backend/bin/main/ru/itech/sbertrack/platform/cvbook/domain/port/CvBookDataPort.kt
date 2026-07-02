package ru.itech.sbertrack.platform.cvbook.domain.port

import ru.itech.sbertrack.platform.cvbook.domain.model.CvBookCandidate
import java.util.UUID

interface CvBookDataPort {
    fun list(): List<CvBookCandidate>
    fun findById(id: UUID): CvBookCandidate?
    fun findByStudentId(studentId: UUID): CvBookCandidate?
    fun save(candidate: CvBookCandidate): CvBookCandidate
}
