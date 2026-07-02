package ru.itech.sbertrack.platform.reflection.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

@Schema(description = "Рефлексия участника")
data class ReflectionResponse(
    val id: UUID,
    val submissionId: UUID,
    val studentId: UUID,
    val answers: Map<String, String>,
    val summary: String,
    val createdAt: Instant,
)
