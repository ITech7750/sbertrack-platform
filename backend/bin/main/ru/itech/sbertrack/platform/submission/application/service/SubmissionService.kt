package ru.itech.sbertrack.platform.submission.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.challengecase.domain.port.CaseDataPort
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.cvbook.domain.model.CvBookCandidate
import ru.itech.sbertrack.platform.cvbook.domain.model.PriorityStatus
import ru.itech.sbertrack.platform.cvbook.domain.port.CvBookDataPort
import ru.itech.sbertrack.platform.portfolio.domain.model.Portfolio
import ru.itech.sbertrack.platform.portfolio.domain.port.PortfolioDataPort
import ru.itech.sbertrack.platform.submission.application.mapper.SubmissionMapper
import ru.itech.sbertrack.platform.submission.domain.model.Submission
import ru.itech.sbertrack.platform.submission.domain.model.SubmissionStatus
import ru.itech.sbertrack.platform.submission.domain.port.SubmissionDataPort
import ru.itech.sbertrack.platform.submission.dto.request.SubmissionCreateRequest
import ru.itech.sbertrack.platform.submission.dto.request.SubmissionUpdateRequest
import ru.itech.sbertrack.platform.submission.dto.response.SubmissionResponse
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort
import java.time.Instant
import java.util.UUID

@Service
class SubmissionService(
    private val submissionDataPort: SubmissionDataPort,
    private val caseDataPort: CaseDataPort,
    private val portfolioDataPort: PortfolioDataPort,
    private val cvBookDataPort: CvBookDataPort,
    private val userDataPort: UserDataPort,
    private val submissionMapper: SubmissionMapper,
    private val authService: AuthService,
) {
    fun list(caseId: UUID?, studentId: UUID?, status: SubmissionStatus?): List<SubmissionResponse> =
        submissionDataPort.list()
            .filter { caseId == null || it.caseId == caseId }
            .filter { studentId == null || it.studentId == studentId }
            .filter { status == null || it.status == status }
            .map(submissionMapper::toResponse)

    fun get(id: UUID): SubmissionResponse =
        findDomain(id).let(submissionMapper::toResponse)

    fun findDomain(id: UUID): Submission =
        submissionDataPort.findById(id) ?: throw NotFoundException("Решение не найдено")

    fun create(request: SubmissionCreateRequest, authorization: String?): SubmissionResponse {
        caseDataPort.findById(request.caseId) ?: throw NotFoundException("Кейс не найден")
        val currentUser = authService.tryCurrentUser(authorization)
        val studentId = request.studentId ?: currentUser?.id ?: userDataPort.findByEmail("student@example.com")!!.id
        val submission = Submission(
            caseId = request.caseId,
            studentId = studentId,
            teamName = request.teamName,
            title = request.title,
            description = request.description,
            artifactUrl = request.artifactUrl,
        )
        return submissionMapper.toResponse(submissionDataPort.save(submission))
    }

    fun update(id: UUID, request: SubmissionUpdateRequest): SubmissionResponse {
        val submission = findDomain(id)
        return submissionMapper.toResponse(
            submissionDataPort.save(
                submission.updateDraft(
                    title = request.title,
                    description = request.description,
                    artifactUrl = request.artifactUrl,
                    teamName = request.teamName,
                ),
            ),
        )
    }

    fun submit(id: UUID): SubmissionResponse {
        val submission = findDomain(id)
        val practicalCase = caseDataPort.findById(submission.caseId) ?: throw NotFoundException("Кейс не найден")
        val scores = practicalCase.competencyWeights.mapValues { (_, weight) -> (55 + weight / 2).coerceIn(0, 100) }
        val submitted = submissionDataPort.save(submission.submit(scores))
        val portfolio = portfolioDataPort.findByStudentId(submitted.studentId)
            ?: Portfolio(
                studentId = submitted.studentId,
                summary = "Портфолио формируется по выполненным практическим кейсам.",
                completedCases = emptyList(),
                competencyProfile = zeroCompetencies(),
                artifacts = emptyList(),
                feedbackHighlights = emptyList(),
                cvBookIncluded = true,
            )
        portfolioDataPort.save(portfolio.includeCompletedCase(practicalCase.title, submitted.artifactUrl, scores))
        upsertCvBookCandidate(submitted.studentId, scores)
        return submissionMapper.toResponse(submitted)
    }

    fun setStatus(id: UUID, status: SubmissionStatus): Submission {
        val submission = findDomain(id)
        val updated = submission.copy(status = status)
        return submissionDataPort.save(updated)
    }

    fun attachFeedback(submissionId: UUID, feedbackId: UUID, status: SubmissionStatus?): Submission {
        val submission = findDomain(submissionId)
        return submissionDataPort.save(submission.addFeedback(feedbackId, status ?: submission.status))
    }

    fun attachReflection(submissionId: UUID, reflectionId: UUID): Submission {
        val submission = findDomain(submissionId)
        return submissionDataPort.save(submission.attachReflection(reflectionId))
    }

    fun markPriority(id: UUID): SubmissionResponse {
        val submission = submissionDataPort.save(findDomain(id).markPriority())
        cvBookDataPort.findByStudentId(submission.studentId)?.let { cvBookDataPort.save(it.markPriority()) }
        return submissionMapper.toResponse(submission)
    }

    private fun upsertCvBookCandidate(studentId: UUID, scores: Map<Competency, Int>) {
        val user = userDataPort.findById(studentId) ?: return
        val existing = cvBookDataPort.findByStudentId(studentId)
        val completedCasesCount = (existing?.completedCasesCount ?: 0).coerceAtLeast(0) + 1
        val averageScore = scores.values.ifEmpty { listOf(0) }.average().toInt()
        val candidate = existing?.copy(
            completedCasesCount = completedCasesCount,
            averageScore = ((existing.averageScore + averageScore) / 2).coerceAtMost(100),
            competencyProfile = mergeCompetencies(existing.competencyProfile, scores),
            lastActivityAt = Instant.now(),
        ) ?: CvBookCandidate(
            studentId = studentId,
            fullName = user.fullName,
            organizationName = user.organizationName,
            completedCasesCount = completedCasesCount,
            averageScore = averageScore,
            competencyProfile = scores,
            tags = listOf("new-submission", "portfolio"),
            priorityStatus = PriorityStatus.REGULAR,
        )
        cvBookDataPort.save(candidate)
    }

    private fun zeroCompetencies(): Map<Competency, Int> =
        Competency.entries.associateWith { 0 }

    private fun mergeCompetencies(current: Map<Competency, Int>, delta: Map<Competency, Int>): Map<Competency, Int> =
        Competency.entries.associateWith { competency ->
            (((current[competency] ?: 0) + (delta[competency] ?: 0)) / 2).coerceAtMost(100)
        }
}
