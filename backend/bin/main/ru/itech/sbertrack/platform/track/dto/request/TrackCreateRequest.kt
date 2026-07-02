package ru.itech.sbertrack.platform.track.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import ru.itech.sbertrack.platform.common.model.Difficulty
import java.util.UUID

@Schema(description = "Создание трека")
data class TrackCreateRequest(
    @field:NotBlank
    val title: String,
    @field:NotBlank
    val description: String,
    val customerId: UUID?,
    val customerName: String?,
    val difficulty: Difficulty,
    @field:NotBlank
    val targetAudience: String,
)
