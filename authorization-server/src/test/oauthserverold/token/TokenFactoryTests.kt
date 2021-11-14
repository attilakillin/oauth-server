package com.bme.jnsbbk.oauthserverold.token

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.jwt.TokenJwtHandler
import com.bme.jnsbbk.oauthserver.token.TokenFactory
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import com.bme.jnsbbk.oauthserver.utils.RandomString
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.Instant

@ExtendWith(MockKExtension::class)
class TokenFactoryTests {
    private val appConfig = mockk<AppConfig>()
    private val jwtHandler = mockk<TokenJwtHandler>()
    private val tokenFactory = TokenFactory(jwtHandler, appConfig)

    private val code = AuthCode(
        value = RandomString.generate(),
        clientId = RandomString.generate(),
        userId = "user id",
        scope = setOf("different", "scope", "values"),
        nonce = null,
        issuedAt = Instant.now(),
        notBefore = Instant.now(),
        expiresAt = null
    )

    private val nbeOffset = 15L
    private val lifespan = 60L

    init {
        every { appConfig.tokens.accessToken.notBeforeOffset } returns nbeOffset
        every { appConfig.tokens.accessToken.lifespan } returns lifespan
        every { appConfig.tokens.refreshToken.notBeforeOffset } returns nbeOffset
        every { appConfig.tokens.refreshToken.lifespan } returns lifespan
    }

    @Test
    fun accessFromCode_createsValidValues() {
        val value = RandomString.generate()
        val token = tokenFactory.accessFromCode(value, code)
        assertEquals(value, token.value)
        assertEquals(code.clientId, token.clientId)
        assertEquals(code.userId, token.userId)
        assertEquals(code.scope, token.scope)
        assertEquals(TokenType.ACCESS, token.type)
    }

    @Test
    fun accessFromCode_abidesByAppConfig() {
        val token = tokenFactory.accessFromCode("value", code)
        assertEquals(nbeOffset, Duration.between(token.issuedAt, token.notBefore).toSeconds())
        assertEquals(lifespan, Duration.between(token.notBefore, token.expiresAt).toSeconds())
    }

    @Test
    fun accessFromCode_abidesByAppConfigWithZeroLifespan() {
        every { appConfig.tokens.accessToken.lifespan } returns 0L
        val token = tokenFactory.accessFromCode("value", code)
        assertNull(token.expiresAt)
    }

    @Test
    fun refreshFromCode_createsValidValues() {
        val value = RandomString.generate()
        val token = tokenFactory.refreshFromCode(value, code)
        assertEquals(value, token.value)
        assertEquals(code.clientId, token.clientId)
        assertEquals(code.userId, token.userId)
        assertEquals(code.scope, token.scope)
        assertEquals(TokenType.REFRESH, token.type)
    }

    @Test
    fun refreshFromCode_abidesByAppConfig() {
        val token = tokenFactory.refreshFromCode("value", code)
        assertEquals(nbeOffset, Duration.between(token.issuedAt, token.notBefore).toSeconds())
        assertEquals(lifespan, Duration.between(token.notBefore, token.expiresAt).toSeconds())
    }

    @Test
    fun refreshFromCode_abidesByAppConfigWithZeroLifespan() {
        every { appConfig.tokens.refreshToken.lifespan } returns 0L
        val token = tokenFactory.refreshFromCode("value", code)
        assertNull(token.expiresAt)
    }

    @Test
    fun accessFromRefresh_requiresRefreshToken() {
        val accessToken = tokenFactory.accessFromCode("value", code)
        assertThrows<Exception> { tokenFactory.accessFromRefresh("value", accessToken) }
    }

    @Test
    fun accessFromRefresh__createsValidValues() {
        val value = RandomString.generate()
        val refreshToken = tokenFactory.refreshFromCode("anything", code)
        val token = tokenFactory.accessFromRefresh(value, refreshToken)

        assertEquals(value, token.value)
        assertEquals(refreshToken.clientId, token.clientId)
        assertEquals(refreshToken.userId, token.userId)
        assertEquals(refreshToken.scope, token.scope)
        assertEquals(TokenType.ACCESS, token.type)
    }

    @Test
    fun accessFromRefresh_abidesByAppConfig() {
        val refreshToken = tokenFactory.refreshFromCode("anything", code)
        val token = tokenFactory.accessFromRefresh("value", refreshToken)

        assertEquals(nbeOffset, Duration.between(token.issuedAt, token.notBefore).toSeconds())
        assertEquals(lifespan, Duration.between(token.notBefore, token.expiresAt).toSeconds())
    }

    @Test
    fun accessFromRefresh_abidesByAppConfigWithZeroLifespan() {
        every { appConfig.tokens.accessToken.lifespan } returns 0L

        val refreshToken = tokenFactory.refreshFromCode("anything", code)
        val token = tokenFactory.accessFromRefresh("value", refreshToken)

        assertNull(token.expiresAt)
    }

    @Test
    fun responseJwtFromTokens_createsValidValues() {
        val refreshValue = "refresh value"
        val jwtValue = "random jwt value to check against"
        every { jwtHandler.createSignedAccess(any()) } returns jwtValue

        val accessToken = tokenFactory.accessFromCode("value", code)
        val refreshToken = tokenFactory.refreshFromCode("refresh value", code)

        val response = tokenFactory.responseJwtFromTokens(accessToken, refreshToken)
        val expiresInValue = Duration.between(Instant.now(), accessToken.expiresAt).seconds

        assertEquals(jwtValue, response.accessToken)
        assertEquals(refreshValue, response.refreshToken)
        assertEquals(code.scope, response.scope)
        assertEquals("Bearer", response.tokenType)
        assertEquals(expiresInValue, response.expiresIn)
    }

    @Test
    fun responseJwtFromTokens_abidesByAppConfigWithZeroLifespan() {
        every { jwtHandler.createSignedAccess(any()) } returns "jwtValue"
        every { appConfig.tokens.accessToken.lifespan } returns 0L
        val accessToken = tokenFactory.accessFromCode("value", code)
        val refreshToken = tokenFactory.refreshFromCode("refresh value", code)

        val response = tokenFactory.responseJwtFromTokens(accessToken, refreshToken)

        assertNull(response.expiresIn)
    }
}
