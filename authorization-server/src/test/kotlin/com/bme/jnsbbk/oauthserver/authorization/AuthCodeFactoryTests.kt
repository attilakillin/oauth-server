package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.utils.RandomString
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration

@ExtendWith(MockKExtension::class)
class AuthCodeFactoryTests {
    private val appConfig = mockk<AppConfig>()
    private val authCodeFactory = AuthCodeFactory(appConfig)

    private val request = AuthRequest(
        clientId = RandomString.generate(),
        redirectUri = "redirect uri",
        responseType = "response type",
        scope = setOf("different", "scope", "values"),
        state = "state",
        nonce = null
    )

    init {
        request.userId = "user id"
    }

    @Test
    fun fromRequest_retainsValueParameter() {
        every { appConfig.tokens.authCode.notBeforeOffset } returns 0
        every { appConfig.tokens.authCode.lifespan } returns 0

        val value = RandomString.generate()
        val code = authCodeFactory.fromRequest(value, request)
        assertEquals(value, code.value)
    }

    @Test
    fun fromRequest_retainsRequestParameterValues() {
        every { appConfig.tokens.authCode.notBeforeOffset } returns 0
        every { appConfig.tokens.authCode.lifespan } returns 0

        val code = authCodeFactory.fromRequest("value", request)
        assertEquals(request.clientId, code.clientId)
        assertEquals(request.userId, code.userId)
        assertEquals(request.scope, code.scope)
    }

    @Test
    fun fromRequest_abidesByAppConfig() {
        val nbeOffset = 15L
        val lifespan = 60L
        every { appConfig.tokens.authCode.notBeforeOffset } returns nbeOffset
        every { appConfig.tokens.authCode.lifespan } returns lifespan

        val code = authCodeFactory.fromRequest("value", request)
        assertEquals(nbeOffset, Duration.between(code.issuedAt, code.notBefore).toSeconds())
        assertEquals(lifespan, Duration.between(code.notBefore, code.expiresAt).toSeconds())
    }

    @Test
    fun fromRequest_abidesByAppConfigWithZeroLifespan() {
        val nbeOffset = 15L
        val lifespan = 0L
        every { appConfig.tokens.authCode.notBeforeOffset } returns nbeOffset
        every { appConfig.tokens.authCode.lifespan } returns lifespan

        val code = authCodeFactory.fromRequest("value", request)
        assertNull(code.expiresAt)
    }
}
