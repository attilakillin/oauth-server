package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.jwt.AccessTokenHandler
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
import java.time.Instant

@ExtendWith(MockKExtension::class)
class TokenFactoryTests {
    private val accessTokenHandler = mockk<AccessTokenHandler>()
    private val appConfig = mockk<AppConfig>()

    private val factory = TokenFactory(accessTokenHandler, appConfig)

    private val code = AuthCode(
        value = "code_value",
        clientId = "client_id",
        userId = "user_id",
        scope = setOf("scope"),
        nonce = null,
        issuedAt = Instant.now(),
        notBefore = Instant.now(),
        expiresAt = null
    )
    private val refresh = Token(
        value = "value",
        type = TokenType.REFRESH,
        clientId = "client_id",
        userId = "user_id",
        scope = setOf("scope"),
        issuedAt = Instant.now(),
        notBefore = Instant.now(),
        expiresAt = null
    )

    /** Test function: accessFromCode() */

    @Test
    fun accessFromCode_createsValidValues() {
        every { appConfig.tokens.accessToken } returns AppConfig.Lifespan(0, 60)

        val token = factory.accessFromCode("value", code)

        Assertions.assertEquals("value", token.value)
        Assertions.assertEquals(TokenType.ACCESS, token.type)
        Assertions.assertEquals(code.clientId, token.clientId)
        Assertions.assertEquals(code.userId, token.userId)
        Assertions.assertEquals(code.scope, token.scope)
    }

    @Test
    fun accessFromCode_createsValidTimestamps() {
        val lifespan = AppConfig.Lifespan(15, 60)
        every { appConfig.tokens.accessToken } returns lifespan

        val token = factory.accessFromCode("value", code)

        Assertions.assertEquals(lifespan.notBeforeOffset,
            Duration.between(token.issuedAt, token.notBefore).toSeconds())
        Assertions.assertEquals(lifespan.lifespan,
            Duration.between(token.notBefore, token.expiresAt).toSeconds())
    }

    @Test
    fun accessFromCode_withNullLifespan() {
        every { appConfig.tokens.accessToken } returns AppConfig.Lifespan(0, 0)

        val token = factory.accessFromCode("value", code)

        Assertions.assertEquals(null, token.expiresAt)
    }

    /** Test function: refreshFromCode() */

    @Test
    fun refreshFromCode_createsValidValues() {
        every { appConfig.tokens.refreshToken } returns AppConfig.Lifespan(0, 60)

        val token = factory.refreshFromCode("value", code)

        Assertions.assertEquals("value", token.value)
        Assertions.assertEquals(TokenType.REFRESH, token.type)
        Assertions.assertEquals(code.clientId, token.clientId)
        Assertions.assertEquals(code.userId, token.userId)
        Assertions.assertEquals(code.scope, token.scope)
    }

    @Test
    fun refreshFromCode_createsValidTimestamps() {
        val lifespan = AppConfig.Lifespan(15, 60)
        every { appConfig.tokens.refreshToken } returns lifespan

        val token = factory.refreshFromCode("value", code)

        Assertions.assertEquals(lifespan.notBeforeOffset,
            Duration.between(token.issuedAt, token.notBefore).toSeconds())
        Assertions.assertEquals(lifespan.lifespan,
            Duration.between(token.notBefore, token.expiresAt).toSeconds())
    }

    @Test
    fun refreshFromCode_withNullLifespan() {
        every { appConfig.tokens.refreshToken } returns AppConfig.Lifespan(0, 0)

        val token = factory.refreshFromCode("value", code)

        Assertions.assertEquals(null, token.expiresAt)
    }

    /** Test function: accessFromRefresh() */

    @Test
    fun accessFromRefresh_createsValidValues() {
        every { appConfig.tokens.accessToken } returns AppConfig.Lifespan(0, 60)

        val token = factory.accessFromRefresh("value", refresh)

        Assertions.assertEquals("value", token.value)
        Assertions.assertEquals(TokenType.ACCESS, token.type)
        Assertions.assertEquals(refresh.clientId, token.clientId)
        Assertions.assertEquals(refresh.userId, token.userId)
        Assertions.assertEquals(refresh.scope, token.scope)
    }

    @Test
    fun accessFromRefresh_createsValidTimestamps() {
        val lifespan = AppConfig.Lifespan(15, 60)
        every { appConfig.tokens.accessToken } returns lifespan

        val token = factory.accessFromRefresh("value", refresh)

        Assertions.assertEquals(lifespan.notBeforeOffset,
            Duration.between(token.issuedAt, token.notBefore).toSeconds())
        Assertions.assertEquals(lifespan.lifespan,
            Duration.between(token.notBefore, token.expiresAt).toSeconds())
    }

    @Test
    fun accessFromRefresh_withNullLifespan() {
        every { appConfig.tokens.accessToken } returns AppConfig.Lifespan(0, 0)

        val token = factory.accessFromRefresh("value", refresh)

        Assertions.assertEquals(null, token.expiresAt)
    }

    @Test
    fun accessFromRefresh_withAccessTokenInsteadOfRefresh() {
        Assertions.assertThrows(Exception::class.java) {
            factory.accessFromRefresh("value", refresh.copy(type = TokenType.ACCESS))
        }
    }

    /** Test function: accessFromRawData() */

    @Test
    fun accessFromRawData_createsValidValues() {
        every { appConfig.tokens.accessToken } returns AppConfig.Lifespan(0, 60)

        val token = factory.accessFromRawData("value", "client_id", "user_id", setOf("scope"))

        Assertions.assertEquals("value", token.value)
        Assertions.assertEquals(TokenType.ACCESS, token.type)
        Assertions.assertEquals("client_id", token.clientId)
        Assertions.assertEquals("user_id", token.userId)
        Assertions.assertEquals(setOf("scope"), token.scope)
    }

    @Test
    fun accessFromRawData_createsValidTimestamps() {
        val lifespan = AppConfig.Lifespan(15, 60)
        every { appConfig.tokens.accessToken } returns lifespan

        val token = factory.accessFromRawData("value", "client_id", "user_id", setOf("scope"))

        Assertions.assertEquals(lifespan.notBeforeOffset,
            Duration.between(token.issuedAt, token.notBefore).toSeconds())
        Assertions.assertEquals(lifespan.lifespan,
            Duration.between(token.notBefore, token.expiresAt).toSeconds())
    }

    @Test
    fun accessFromRawData_withNullLifespan() {
        every { appConfig.tokens.accessToken } returns AppConfig.Lifespan(0, 0)

        val token = factory.accessFromRawData("value", "client_id", "user_id", setOf("scope"))

        Assertions.assertEquals(null, token.expiresAt)
    }

    /** Test function: responseJwtFromTokens() */

    @Test
    fun responseJwtFromTokens_createsValidValuesWithNoExpiration() {
        val access = refresh.copy(type = TokenType.ACCESS)

        every { accessTokenHandler.createToken(access) } returns "jwt_string"

        val response = factory.responseJwtFromTokens(access, refresh)

        Assertions.assertEquals("jwt_string", response.accessToken)
        Assertions.assertEquals("Bearer", response.tokenType)
        Assertions.assertEquals(refresh.value, response.refreshToken)
        Assertions.assertEquals(access.scope, response.scope)
        Assertions.assertEquals(null, response.expiresIn)
    }

    @Test
    fun responseJwtFromTokens_createsValidValuesWithExpiration() {
        val now = Instant.now()
        val access = refresh.copy(
            type = TokenType.ACCESS,
            expiresAt = now.plusSeconds(60)
        )

        every { accessTokenHandler.createToken(access) } returns "jwt_string"

        val response = factory.responseJwtFromTokens(access, refresh)

        Assertions.assertNotNull(response.expiresIn)
        Assertions.assertTrue(response.expiresIn!! > 0L)
    }
}
