package ru.itech.sbertrack.platform.auth.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.auth.dto.request.SignInRequest
import ru.itech.sbertrack.platform.auth.dto.request.SignUpRequest
import ru.itech.sbertrack.platform.auth.dto.response.UserSessionResponse
import ru.itech.sbertrack.platform.common.exception.ConflictException
import ru.itech.sbertrack.platform.common.exception.ForbiddenException
import ru.itech.sbertrack.platform.common.exception.UnauthorizedException
import ru.itech.sbertrack.platform.user.application.mapper.UserMapper
import ru.itech.sbertrack.platform.user.domain.model.StudentType
import ru.itech.sbertrack.platform.user.domain.model.User
import ru.itech.sbertrack.platform.user.domain.model.UserRole
import ru.itech.sbertrack.platform.user.domain.model.UserStatus
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort
import java.util.UUID

@Service
class AuthService(
    private val userDataPort: UserDataPort,
    private val userMapper: UserMapper,
) {
    fun signIn(request: SignInRequest): UserSessionResponse {
        val user = userDataPort.findByEmail(request.email) ?: throw UnauthorizedException("Неверный логин или пароль")
        if (!userDataPort.verifyPassword(request.email, request.password)) {
            throw UnauthorizedException("Неверный логин или пароль")
        }
        if (user.status != UserStatus.ACTIVE) {
            throw ForbiddenException("Пользователь не активен")
        }
        return session(user)
    }

    fun signUp(request: SignUpRequest): UserSessionResponse {
        if (request.role !in setOf(UserRole.STUDENT, UserRole.CUSTOMER)) {
            throw ForbiddenException("Публичная регистрация доступна только студентам и заказчикам")
        }
        if (userDataPort.findByEmail(request.email) != null) {
            throw ConflictException("Пользователь с таким email уже существует")
        }
        val user = User(
            fullName = request.fullName,
            email = request.email,
            role = request.role,
            organizationName = request.organizationName,
            studentType = if (request.role == UserRole.STUDENT) request.studentType else StudentType.NONE,
        )
        return session(userDataPort.save(user, request.password))
    }

    fun session(authorization: String?): UserSessionResponse =
        session(currentUser(authorization))

    fun currentUser(authorization: String?): User {
        val rawToken = authorization
            ?.removePrefix("Bearer ")
            ?.takeIf { it.startsWith(TOKEN_PREFIX) }
            ?: throw UnauthorizedException()
        val userId = UUID.fromString(rawToken.removePrefix(TOKEN_PREFIX))
        return userDataPort.findById(userId) ?: throw UnauthorizedException()
    }

    fun tryCurrentUser(authorization: String?): User? =
        runCatching { currentUser(authorization) }.getOrNull()

    private fun session(user: User): UserSessionResponse =
        UserSessionResponse(
            token = "$TOKEN_PREFIX${user.id}",
            user = userMapper.toResponse(user),
        )

    private companion object {
        const val TOKEN_PREFIX = "fake-token-"
    }
}
