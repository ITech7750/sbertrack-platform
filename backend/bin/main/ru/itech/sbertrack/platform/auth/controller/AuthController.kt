package ru.itech.sbertrack.platform.auth.controller

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itech.sbertrack.platform.auth.application.service.AuthService
import ru.itech.sbertrack.platform.auth.dto.request.SignInRequest
import ru.itech.sbertrack.platform.auth.dto.request.SignUpRequest
import ru.itech.sbertrack.platform.auth.dto.response.UserSessionResponse

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/sign-up")
    fun signUp(@Valid @RequestBody request: SignUpRequest): UserSessionResponse =
        authService.signUp(request)

    @PostMapping("/sign-in")
    fun signIn(@Valid @RequestBody request: SignInRequest): UserSessionResponse =
        authService.signIn(request)

    @GetMapping("/session")
    fun session(@RequestHeader("Authorization", required = false) authorization: String?): UserSessionResponse =
        authService.session(authorization)
}
