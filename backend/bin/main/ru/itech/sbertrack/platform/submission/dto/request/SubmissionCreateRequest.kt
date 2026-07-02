package ru.itech.sbertrack.platform.submission.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.util.UUID

@Schema(description = "Создание черновика решения")
data class SubmissionCreateRequest(
    val caseId: UUID,
    val studentId: UUID?,
    val teamName: String?,
    @field:NotBlank
    val title: String,
    val description: String = "",
    val artifactUrl: String?,
)
