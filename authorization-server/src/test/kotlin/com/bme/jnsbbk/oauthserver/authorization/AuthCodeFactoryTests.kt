package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.config.AppConfig
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@ExtendWith(MockKExtension::class)
class AuthCodeFactoryTests {
    private val config = mockk<AppConfig.Lifespan>()

    private val appConfig = AppConfig(tokens = AppConfig.Tokens(authCode = config))
    private val authCodeFactory = AuthCodeFactory(appConfig)

    private val request = AuthRequest(
        clientId = "client_id",
        redirectUri = "redirect_uri",
        responseType = "code",
        scope = setOf("scope"),
        state = "state",
        nonce = "nonce"
    ).apply { userId = "user_id" }

    @Test
    fun fromRequest_withValidRequest_keepsIdentifiers() {
        every { config.notBeforeOffset } returns 0L
        every { config.lifespan } returns 0L

        val value = "code_value"
        val code = authCodeFactory.fromRequest(value, request)
        Assertions.assertEquals(value, code.value)
        Assertions.assertEquals(request.clientId, code.clientId)
        Assertions.assertEquals(request.userId, code.userId)
        Assertions.assertEquals(request.scope, code.scope)
    }

    @Test
    fun fromRequest_withValidRequest_keepsLifespanWithExpiration() {
        val nbe = 15L
        val exp = 60L
        every { config.notBeforeOffset } returns nbe
        every { config.lifespan } returns exp

        val code = authCodeFactory.fromRequest("", request)
        Assertions.assertEquals(nbe, Duration.between(code.issuedAt, code.notBefore).toSeconds())
        Assertions.assertEquals(exp, Duration.between(code.notBefore, code.expiresAt).toSeconds())
    }

    @Test
    fun fromRequest_withValidRequest_keepsLifespanWithoutExpiration() {
        val nbe = 15L
        every { config.notBeforeOffset } returns nbe
        every { config.lifespan } returns 0L

        val code = authCodeFactory.fromRequest("", request)
        Assertions.assertEquals(nbe, Duration.between(code.issuedAt, code.notBefore).toSeconds())
        Assertions.assertEquals(null, code.expiresAt)
    }
}
