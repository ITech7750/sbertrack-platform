package ru.itech.sbertrack.platform.challengecase.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.challengecase.application.mapper.CaseMapper
import ru.itech.sbertrack.platform.challengecase.domain.model.CaseStatus
import ru.itech.sbertrack.platform.challengecase.domain.model.PracticalCase
import ru.itech.sbertrack.platform.challengecase.domain.port.CaseDataPort
import ru.itech.sbertrack.platform.challengecase.dto.request.CaseCreateRequest
import ru.itech.sbertrack.platform.challengecase.dto.request.CaseUpdateRequest
import ru.itech.sbertrack.platform.challengecase.dto.response.CaseResponse
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.common.model.Difficulty
import ru.itech.sbertrack.platform.track.application.service.TrackService
import ru.itech.sbertrack.platform.track.domain.port.TrackDataPort
import ru.itech.sbertrack.platform.user.domain.model.UserRole
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort
import java.util.UUID

@Service
class CaseService(
    private val caseDataPort: CaseDataPort,
    private val trackDataPort: TrackDataPort,
    private val userDataPort: UserDataPort,
    private val caseMapper: CaseMapper,
    private val trackService: TrackService,
    private val authService: AuthService,
) {
    fun list(
        trackId: UUID?,
        status: CaseStatus?,
        difficulty: Difficulty?,
        tag: String?,
        competency: Competency?,
    ): List<CaseResponse> =
        caseDataPort.list()
            .filter { trackId == null || it.trackId == trackId }
            .filter { status == null || it.status == status }
            .filter { difficulty == null || it.difficulty == difficulty }
            .filter { tag.isNullOrBlank() || it.tags.any { caseTag -> caseTag.contains(tag, ignoreCase = true) } }
            .filter { competency == null || (it.competencyWeights[competency] ?: 0) > 0 }
            .map(caseMapper::toResponse)

    fun get(id: UUID): CaseResponse =
        findDomain(id).let(caseMapper::toResponse)

    fun findDomain(id: UUID): PracticalCase =
        caseDataPort.findById(id) ?: throw NotFoundException("Кейс не найден")

    fun create(request: CaseCreateRequest, authorization: String?): CaseResponse {
        trackDataPort.findById(request.trackId) ?: throw NotFoundException("Трек не найден")
        val customer = resolveCustomer(request.customerId, request.customerName, authorization)
        val practicalCase = PracticalCase(
            trackId = request.trackId,
            title = request.title,
            shortDescription = request.shortDescription,
            fullDescription = request.fullDescription,
            customerId = customer.first,
            customerName = customer.second,
            difficulty = request.difficulty,
            participantLimit = request.participantLimit,
            expectedResult = request.expectedResult,
            feedbackMode = request.feedbackMode,
            competencyWeights = request.competencyWeights,
            tags = request.tags,
            deadline = request.deadline,
        )
        val saved = caseDataPort.save(practicalCase)
        trackService.attachCase(saved.trackId, saved.id)
        return caseMapper.toResponse(saved)
    }

    fun update(id: UUID, request: CaseUpdateRequest): CaseResponse {
        val practicalCase = findDomain(id)
        return caseMapper.toResponse(
            caseDataPort.save(
                practicalCase.update(
                    title = request.title,
                    shortDescription = request.shortDescription,
                    fullDescription = request.fullDescription,
                    difficulty = request.difficulty,
                    participantLimit = request.participantLimit,
                    expectedResult = request.expectedResult,
                    feedbackMode = request.feedbackMode,
                    competencyWeights = request.competencyWeights,
                    tags = request.tags,
                    deadline = request.deadline,
                ),
            ),
        )
    }

    fun sendToModeration(id: UUID): CaseResponse =
        caseMapper.toResponse(caseDataPort.save(findDomain(id).sendToModeration()))

    fun publish(id: UUID): CaseResponse =
        caseMapper.toResponse(caseDataPort.save(findDomain(id).publish()))

    fun archive(id: UUID): CaseResponse =
        caseMapper.toResponse(caseDataPort.save(findDomain(id).archive()))

    fun rejectModeration(id: UUID): CaseResponse =
        caseMapper.toResponse(caseDataPort.save(findDomain(id).returnToDraft()))

    private fun resolveCustomer(customerId: UUID?, customerName: String?, authorization: String?): Pair<UUID, String> {
        val current = authService.tryCurrentUser(authorization)
        val explicit = customerId?.let { userDataPort.findById(it) }
        val fallback = userDataPort.list().first { it.role == UserRole.CUSTOMER }
        val customer = explicit ?: current?.takeIf { it.role == UserRole.CUSTOMER } ?: fallback
        return customer.id to (customerName?.takeIf { it.isNotBlank() } ?: customer.organizationName ?: customer.fullName)
    }
}
