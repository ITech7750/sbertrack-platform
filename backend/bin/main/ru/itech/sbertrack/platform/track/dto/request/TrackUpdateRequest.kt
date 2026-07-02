package ru.itech.sbertrack.platform.track.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import ru.itech.sbertrack.platform.common.model.Difficulty
import ru.itech.sbertrack.platform.track.domain.model.TrackStatus

@Schema(description = "Обновление трека")
data class TrackUpdateRequest(
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val description: String,
    val difficulty: Difficulty,
    val status: TrackStatus,
    @field:NotBlank
    val targetAudience: String,
)
