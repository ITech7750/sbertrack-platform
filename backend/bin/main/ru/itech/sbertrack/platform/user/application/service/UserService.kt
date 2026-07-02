package ru.itech.sbertrack.platform.user.application.service

import org.springframework.stereotype.Service
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.common.exception.NotFoundException
import ru.itech.sbertrack.platform.user.application.mapper.UserMapper
import ru.itech.sbertrack.platform.user.dto.response.UserResponse
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort
import java.util.UUID

@Service
class UserService(
    private val userDataPort: UserDataPort,
    private val userMapper: UserMapper,
    private val authService: AuthService,
) {
    fun current(authorization: String?): UserResponse =
        userMapper.toResponse(authService.currentUser(authorization))

    fun list(): List<UserResponse> =
        userDataPort.list().map(userMapper::toResponse)

    fun get(id: UUID): UserResponse =
        userDataPort.findById(id)?.let(userMapper::toResponse)
            ?: throw NotFoundException("Пользователь не найден")
}
