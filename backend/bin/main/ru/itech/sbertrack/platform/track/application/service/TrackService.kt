package ru.itech.sbertrack.platform.track.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.challengecase.domain.port.CaseDataPort
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.common.model.Competency
import ru.itech.sbertrack.platform.common.model.Difficulty
import ru.itech.sbertrack.platform.track.application.mapper.TrackMapper
import ru.itech.sbertrack.platform.track.domain.model.Track
import ru.itech.sbertrack.platform.track.domain.port.TrackDataPort
import ru.itech.sbertrack.platform.track.dto.request.TrackCreateRequest
import ru.itech.sbertrack.platform.track.dto.request.TrackUpdateRequest
import ru.itech.sbertrack.platform.track.dto.response.TrackResponse
import ru.itech.sbertrack.platform.user.domain.model.UserRole
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort
import java.util.UUID

@Service
class TrackService(
    private val trackDataPort: TrackDataPort,
    private val caseDataPort: CaseDataPort,
    private val userDataPort: UserDataPort,
    private val trackMapper: TrackMapper,
    private val authService: AuthService,
) {
    fun list(
        difficulty: Difficulty?,
        targetAudience: String?,
        competency: Competency?,
        customerName: String?,
    ): List<TrackResponse> =
        trackDataPort.list()
            .filter { difficulty == null || it.difficulty == difficulty }
            .filter { targetAudience.isNullOrBlank() || it.targetAudience.contains(targetAudience, ignoreCase = true) }
            .filter { customerName.isNullOrBlank() || it.customerName.contains(customerName, ignoreCase = true) }
            .filter { track ->
                competency == null || track.caseIds
                    .mapNotNull(caseDataPort::findById)
                    .any { (it.competencyWeights[competency] ?: 0) > 0 }
            }
            .map(trackMapper::toResponse)

    fun get(id: UUID): TrackResponse =
        trackDataPort.findById(id)?.let(trackMapper::toResponse)
            ?: throw NotFoundException("Трек не найден")

    fun create(request: TrackCreateRequest, authorization: String?): TrackResponse {
        val customer = resolveCustomer(request.customerId, request.customerName, authorization)
        val track = Track(
            title = request.title,
            description = request.description,
            customerId = customer.first,
            customerName = customer.second,
            difficulty = request.difficulty,
            targetAudience = request.targetAudience,
        )
        return trackMapper.toResponse(trackDataPort.save(track))
    }

    fun update(id: UUID, request: TrackUpdateRequest): TrackResponse {
        val track = trackDataPort.findById(id) ?: throw NotFoundException("Трек не найден")
        return trackMapper.toResponse(
            trackDataPort.save(
                track.update(
                    title = request.title,
                    description = request.description,
                    difficulty = request.difficulty,
                    status = request.status,
                    targetAudience = request.targetAudience,
                ),
            ),
        )
    }

    fun attachCase(trackId: UUID, caseId: UUID) {
        val track = trackDataPort.findById(trackId) ?: throw NotFoundException("Трек не найден")
        trackDataPort.save(track.addCase(caseId))
    }

    private fun resolveCustomer(customerId: UUID?, customerName: String?, authorization: String?): Pair<UUID, String> {
        val current = authService.tryCurrentUser(authorization)
        val explicit = customerId?.let { userDataPort.findById(it) }
        val fallback = userDataPort.list().first { it.role == UserRole.CUSTOMER }
        val customer = explicit ?: current?.takeIf { it.role == UserRole.CUSTOMER } ?: fallback
        return customer.id to (customerName?.takeIf { it.isNotBlank() } ?: customer.organizationName ?: customer.fullName)
    }
}
