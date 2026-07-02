package ru.itech.sbertrack.platform.feedback.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.cvbook.domain.port.CvBookDataPort
import ru.itech.sbertrack.platform.feedback.application.mapper.FeedbackMapper
import ru.itech.sbertrack.platform.feedback.domain.model.Feedback
import ru.itech.sbertrack.platform.feedback.domain.port.FeedbackDataPort
import ru.itech.sbertrack.platform.feedback.dto.request.FeedbackCreateRequest
import ru.itech.sbertrack.platform.feedback.dto.response.FeedbackResponse
import ru.itech.sbertrack.platform.portfolio.domain.port.PortfolioDataPort
import ru.itech.sbertrack.platform.submission.application.service.SubmissionService
import ru.itech.sbertrack.platform.submission.domain.model.SubmissionStatus
import java.util.UUID

@Service
class FeedbackService(
    private val feedbackDataPort: FeedbackDataPort,
    private val portfolioDataPort: PortfolioDataPort,
    private val cvBookDataPort: CvBookDataPort,
    private val submissionService: SubmissionService,
    private val feedbackMapper: FeedbackMapper,
) {
    fun listBySubmission(submissionId: UUID): List<FeedbackResponse> =
        feedbackDataPort.listBySubmissionId(submissionId).map(feedbackMapper::toResponse)

    fun listAll(): List<FeedbackResponse> =
        feedbackDataPort.list().map(feedbackMapper::toResponse)

    fun create(request: FeedbackCreateRequest): FeedbackResponse {
        val submission = submissionService.findDomain(request.submissionId)
        val feedback = Feedback(
            submissionId = request.submissionId,
            authorType = request.authorType,
            authorName = request.authorName,
            text = request.text,
            recommendations = request.recommendations,
            competencyDelta = request.competencyDelta,
        )
        val saved = feedbackDataPort.save(feedback)
        val targetStatus = request.targetSubmissionStatus
        submissionService.attachFeedback(submission.id, saved.id, targetStatus)
        portfolioDataPort.findByStudentId(submission.studentId)
            ?.addFeedbackHighlight(saved.text)
            ?.let(portfolioDataPort::save)
        if (targetStatus == SubmissionStatus.PRIORITY_CANDIDATE) {
            cvBookDataPort.findByStudentId(submission.studentId)?.let { cvBookDataPort.save(it.markPriority()) }
        }
        return feedbackMapper.toResponse(saved)
    }

    fun get(id: UUID): FeedbackResponse =
        feedbackDataPort.findById(id)?.let(feedbackMapper::toResponse)
            ?: throw NotFoundException("Feedback не найден")
}
