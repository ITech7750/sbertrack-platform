package ru.itech.sbertrack.platform.user.domain.port

import ru.itech.sbertrack.platform.user.domain.model.User
import java.util.UUID

interface UserDataPort {
    fun list(): List<User>
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun verifyPassword(email: String, password: String): Boolean
    fun save(user: User, password: String? = null): User
}
