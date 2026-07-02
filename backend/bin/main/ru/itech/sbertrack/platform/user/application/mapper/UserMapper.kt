package ru.itech.sbertrack.platform.user.application.mapper

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.user.domain.model.User
import ru.itech.sbertrack.platform.user.dto.response.UserResponse

@Component
class UserMapper {
    fun toResponse(user: User): UserResponse =
        UserResponse(
            id = user.id,
            fullName = user.fullName,
            email = user.email,
            role = user.role,
            organizationName = user.organizationName,
            studentType = user.studentType,
            status = user.status,
            createdAt = user.createdAt,
        )
}
