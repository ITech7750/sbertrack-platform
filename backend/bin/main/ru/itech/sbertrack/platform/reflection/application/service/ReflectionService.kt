package ru.itech.sbertrack.platform.reflection.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.reflection.application.mapper.ReflectionMapper
import ru.itech.sbertrack.platform.reflection.domain.model.Reflection
import ru.itech.sbertrack.platform.reflection.domain.port.ReflectionDataPort
import ru.itech.sbertrack.platform.reflection.dto.request.ReflectionCreateRequest
import ru.itech.sbertrack.platform.reflection.dto.response.ReflectionResponse
import ru.itech.sbertrack.platform.submission.application.service.SubmissionService
import java.util.UUID

@Service
class ReflectionService(
    private val reflectionDataPort: ReflectionDataPort,
    private val submissionService: SubmissionService,
    private val reflectionMapper: ReflectionMapper,
    private val authService: AuthService,
) {
    fun getBySubmission(submissionId: UUID): ReflectionResponse =
        reflectionDataPort.findBySubmissionId(submissionId)?.let(reflectionMapper::toResponse)
            ?: throw NotFoundException("Рефлексия не найдена")

    fun create(request: ReflectionCreateRequest, authorization: String?): ReflectionResponse {
        val submission = submissionService.findDomain(request.submissionId)
        val studentId = request.studentId ?: authService.tryCurrentUser(authorization)?.id ?: submission.studentId
        val reflection = Reflection(
            submissionId = request.submissionId,
            studentId = studentId,
            answers = request.answers,
            summary = request.summary,
        )
        val saved = reflectionDataPort.save(reflection)
        submissionService.attachReflection(submission.id, saved.id)
        return reflectionMapper.toResponse(saved)
    }
}
