package ru.itech.sbertrack.platform.user.infrastructure.adapter

import org.springframework.stereotype.Component
import ru.itech.sbertrack.platform.common.infrastructure.MockPlatformDataStore
import ru.itech.sbertrack.platform.user.domain.model.User
import ru.itech.sbertrack.platform.user.domain.port.UserDataPort
import java.util.UUID

@Component
class UserMockAdapter(
    private val dataStore: MockPlatformDataStore,
) : UserDataPort {
    override fun list(): List<User> =
        dataStore.users.values.sortedBy { it.createdAt }

    override fun findById(id: UUID): User? =
        dataStore.users[id]

    override fun findByEmail(email: String): User? =
        dataStore.users.values.firstOrNull { it.email.equals(email, ignoreCase = true) }

    override fun verifyPassword(email: String, password: String): Boolean =
        dataStore.passwordsByEmail[email.lowercase()] == password

    override fun save(user: User, password: String?): User {
        dataStore.users[user.id] = user
        if (password != null) {
            dataStore.passwordsByEmail[user.email.lowercase()] = password
        }
        return user
    }
}
