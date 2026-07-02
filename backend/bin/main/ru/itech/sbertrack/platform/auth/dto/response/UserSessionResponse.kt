package ru.itech.sbertrack.platform.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import ru.itech.sbertrack.platform.user.dto.response.UserResponse

@Schema(description = "Моковая пользовательская сессия")
data class UserSessionResponse(
    val token: String,
    val user: UserResponse,
)
