package ru.itech.sbertrack.platform.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import ru.itech.sbertrack.platform.user.domain.model.StudentType
import ru.itech.sbertrack.platform.user.domain.model.UserRole

@Schema(description = "Запрос регистрации студента или заказчика")
data class SignUpRequest(
    @field:NotBlank
    val fullName: String,
    @field:Email
    val email: String,
    @field:NotBlank
    val password: String,
    val role: UserRole,
    val organizationName: String?,
    val studentType: StudentType = StudentType.NONE,
)
