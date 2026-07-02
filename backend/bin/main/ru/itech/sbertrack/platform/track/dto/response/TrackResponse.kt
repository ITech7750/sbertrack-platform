package ru.itech.sbertrack.platform.track.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.common.model.Difficulty
import ru.itech.sbertrack.platform.track.domain.model.TrackStatus
import java.time.Instant
import java.util.UUID

@Schema(description = "Трек практических кейсов")
data class TrackResponse(
    val id: UUID,
    val title: String,
    val description: String,
    val customerId: UUID,
    val customerName: String,
    val difficulty: Difficulty,
    val status: TrackStatus,
    val targetAudience: String,
    val caseIds: List<UUID>,
    val createdAt: Instant,
)
