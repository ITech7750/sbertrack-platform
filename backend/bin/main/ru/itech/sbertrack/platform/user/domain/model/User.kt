package ru.itech.sbertrack.platform.user.domain.model

import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val fullName: String,
    val email: String,
    val role: UserRole,
    val organizationName: String? = null,
    val studentType: StudentType = StudentType.NONE,
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: Instant = Instant.now(),
) {
    init {
        require(fullName.isNotBlank()) { "Имя пользователя обязательно" }
        require(email.contains("@")) { "Email должен быть корректным" }
        if (role == UserRole.STUDENT) {
            require(studentType != StudentType.NONE) { "Для студента нужно указать тип" }
        }
        if (role == UserRole.CUSTOMER) {
            require(!organizationName.isNullOrBlank()) { "Для заказчика нужна организация" }
        }
    }

    fun block(): User = copy(status = UserStatus.BLOCKED)

    fun activate(): User = copy(status = UserStatus.ACTIVE)
}
