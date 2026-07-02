package ru.itech.sbertrack.platform.cvbook.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.cvbook.application.mapper.CvBookMapper
import ru.itech.sbertrack.platform.cvbook.domain.model.PriorityStatus
import ru.itech.sbertrack.platform.cvbook.domain.port.CvBookDataPort
import ru.itech.sbertrack.platform.cvbook.dto.response.CvBookCandidateResponse
import java.util.UUID

@Service
class CvBookService(
    private val cvBookDataPort: CvBookDataPort,
    private val cvBookMapper: CvBookMapper,
) {
    fun list(
        competency: Competency?,
        minScore: Int?,
        caseTag: String?,
        organizationName: String?,
        priorityOnly: Boolean?,
        completedCasesMin: Int?,
    ): List<CvBookCandidateResponse> =
        cvBookDataPort.list()
            .filter { competency == null || (it.competencyProfile[competency] ?: 0) >= (minScore ?: 0) }
            .filter { competency != null || minScore == null || it.averageScore >= minScore }
            .filter { caseTag.isNullOrBlank() || it.tags.any { tag -> tag.contains(caseTag, ignoreCase = true) } }
            .filter { organizationName.isNullOrBlank() || it.organizationName?.contains(organizationName, ignoreCase = true) == true }
            .filter { priorityOnly != true || it.priorityStatus == PriorityStatus.PRIORITY }
            .filter { completedCasesMin == null || it.completedCasesCount >= completedCasesMin }
            .map(cvBookMapper::toResponse)

    fun get(id: UUID): CvBookCandidateResponse =
        cvBookDataPort.findById(id)?.let(cvBookMapper::toResponse)
            ?: throw NotFoundException("Кандидат CV-book не найден")

    fun markPriorityByStudentId(studentId: UUID) {
        cvBookDataPort.findByStudentId(studentId)?.let { cvBookDataPort.save(it.markPriority()) }
    }
}
