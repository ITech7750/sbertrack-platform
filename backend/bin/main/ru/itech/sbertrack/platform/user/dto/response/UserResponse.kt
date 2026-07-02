package ru.itech.sbertrack.platform.user.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.user.domain.model.StudentType
import ru.itech.sbertrack.platform.user.domain.model.UserRole
import ru.itech.sbertrack.platform.user.domain.model.UserStatus
import java.time.Instant
import java.util.UUID

@Schema(description = "Пользователь платформы")
data class UserResponse(
    val id: UUID,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val organizationName: String?,
    val studentType: StudentType,
    val status: UserStatus,
    val createdAt: Instant,
)
