package ru.itech.sbertrack.platform.track.application.mapper

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.track.domain.model.Track
import ru.itech.sbertrack.platform.track.dto.response.TrackResponse

@Component
class TrackMapper {
    fun toResponse(track: Track): TrackResponse =
        TrackResponse(
            id = track.id,
            title = track.title,
            description = track.description,
            customerId = track.customerId,
            customerName = track.customerName,
            difficulty = track.difficulty,
            status = track.status,
            targetAudience = track.targetAudience,
            caseIds = track.caseIds,
            createdAt = track.createdAt,
        )
}
