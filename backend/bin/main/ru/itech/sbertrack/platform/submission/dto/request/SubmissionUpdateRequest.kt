package ru.itech.sbertrack.platform.submission.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Обновление черновика решения")
data class SubmissionUpdateRequest(
    val teamName: String?,
    @field:NotBlank
    val title: String,
    val description: String,
    val artifactUrl: String?,
)
