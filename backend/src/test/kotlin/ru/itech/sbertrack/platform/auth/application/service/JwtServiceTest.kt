package ru.itech.sbertrack.platform.auth.application.service

import org.springframework.mock.env.MockEnvironment
import ru.itech.sbertrack.platform.common.exception.UnauthorizedException
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class JwtServiceTest {
    private val secret = "test-only-secret-that-is-long-enough-for-hs256"
    private val devEnvironment = MockEnvironment()
    private val prodEnvironment = MockEnvironment().apply { setActiveProfiles("prod") }
    private val jwtService = JwtService(secret, ttlHours = 24, environment = devEnvironment)

    @Test
    fun `issued token round-trips back to the same user id`() {
        val userId = UUID.randomUUID()

        val token = jwtService.issue(userId)

        assertEquals(userId, jwtService.parseUserId(token))
    }

    @Test
    fun `tokens for different users differ`() {
        val first = jwtService.issue(UUID.randomUUID())
        val second = jwtService.issue(UUID.randomUUID())

        assertNotEquals(first, second)
    }

    @Test
    fun `tampered token is rejected`() {
        val token = jwtService.issue(UUID.randomUUID())
        // The final Base64URL character may only change unused padding bits.
        // Changing the first signature character always changes signed bytes.
        val signatureStart = token.lastIndexOf('.') + 1
        val replacement = if (token[signatureStart] == 'A') 'B' else 'A'
        val tampered = token.replaceRange(signatureStart, signatureStart + 1, replacement.toString())

        assertFailsWith<UnauthorizedException> { jwtService.parseUserId(tampered) }
    }

    @Test
    fun `garbage token is rejected`() {
        assertFailsWith<UnauthorizedException> { jwtService.parseUserId("not-a-jwt") }
    }

    @Test
    fun `token signed with a different secret is rejected`() {
        val otherService = JwtService(
            "a-completely-different-secret-of-sufficient-length",
            ttlHours = 24,
            environment = devEnvironment,
        )
        val token = otherService.issue(UUID.randomUUID())

        assertFailsWith<UnauthorizedException> { jwtService.parseUserId(token) }
    }

    @Test
    fun `expired token is rejected`() {
        val expiredService = JwtService(secret, ttlHours = 0, environment = devEnvironment)
        val token = expiredService.issue(UUID.randomUUID())

        Thread.sleep(1_100)

        assertFailsWith<UnauthorizedException> { jwtService.parseUserId(token) }
    }

    @Test
    fun `dev placeholder secret is rejected under the prod profile`() {
        assertFailsWith<IllegalArgumentException> {
            JwtService(JwtService.DEV_DEFAULT_SECRET, ttlHours = 24, environment = prodEnvironment)
        }
    }

    @Test
    fun `dev placeholder secret is allowed outside the prod profile`() {
        JwtService(JwtService.DEV_DEFAULT_SECRET, ttlHours = 24, environment = devEnvironment)
    }

    @Test
    fun `secret shorter than 32 bytes is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            JwtService("too-short", ttlHours = 24, environment = devEnvironment)
        }
    }
}
