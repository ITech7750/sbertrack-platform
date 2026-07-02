package ru.itech.sbertrack.platform.user.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.user.application.service.UserService
import ru.itech.sbertrack.platform.user.dto.response.UserResponse
import java.util.UUID

@Tag(name = "Users")
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun me(@RequestHeader("Authorization", required = false) authorization: String?): UserResponse =
        userService.current(authorization)

    @GetMapping
    fun list(): List<UserResponse> =
        userService.list()

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): UserResponse =
        userService.get(id)
}
