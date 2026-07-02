package ru.itech.sbertrack.platform.reflection.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import java.util.UUID

@Schema(description = "Создание рефлексии")
data class ReflectionCreateRequest(
    val submissionId: UUID,
    val studentId: UUID?,
    val answers: Map<String, String>,
    @field:NotBlank
    val summary: String,
)
