package ru.itech.sbertrack.platform.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(description = "Запрос входа")
data class SignInRequest(
    @field:Email
    val email: String,
    @field:NotBlank
    val password: String,
)
