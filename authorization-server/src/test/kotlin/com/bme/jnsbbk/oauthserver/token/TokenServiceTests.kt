package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.jwt.AccessTokenHandler
import com.bme.jnsbbk.oauthserver.jwt.IdTokenHandler
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenResponse
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.utils.getIssuerString
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
class TokenServiceTests {
    private val accessTokenHandler = mockk<AccessTokenHandler>()
    private val idTokenHandler = mockk<IdTokenHandler>()
    private val tokenFactory = mockk<TokenFactory>()
    private val tokenRepository = mockk<TokenRepository>()
    private val userService = mockk<UserService>()

    init {
        mockkStatic(::getIssuerString)
    }

    private val service = TokenService(accessTokenHandler, idTokenHandler, tokenFactory, tokenRepository, userService)

    private val token = Token(
        value = "value",
        type = TokenType.ACCESS,
        clientId = "client_id",
        userId = "user_id",
        scope = setOf("scope"),
        issuedAt = Instant.now(),
        notBefore = Instant.now(),
        expiresAt = null
    )
    private val response = TokenResponse(
        accessToken = "access_token",
        refreshToken = "refresh_token",
        tokenType = "Bearer",
        scope = setOf("scope"),
        expiresIn = null
    )
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
    private val user = User("user_id", "username", "password")

    /** Test function: convertFromJwt() */

    @Test
    fun convertFromJwt_worksAsExpected() {
        every { accessTokenHandler.convertToValidToken(any()) } returns token
        Assertions.assertNotNull(service.convertFromJwt("jwt"))

        every { accessTokenHandler.convertToValidToken(any()) } returns null
        Assertions.assertNull(service.convertFromJwt("jwt"))
    }

    /** Test function: createResponseFromAuthCode() */

    @Test
    fun createResponseFromAuthCode_withValidValues() {
        every { tokenRepository.existsById(any()) } returns false
        every { tokenFactory.accessFromCode(any(), code) } returns token
        every { tokenFactory.refreshFromCode(any(), code) } returns token.copy(type = TokenType.REFRESH)
        every { tokenRepository.save(any()) } answers { firstArg() }
        every { tokenFactory.responseJwtFromTokens(any(), any()) } returns response
        every { userService.getUserById(user.id) } returns user

        Assertions.assertEquals(response, service.createResponseFromAuthCode(code))

        verify { tokenFactory.accessFromCode(any(), code) }
        verify { tokenFactory.refreshFromCode(any(), code) }
        verify(exactly = 2) { tokenRepository.save(any()) }
        verify { tokenFactory.responseJwtFromTokens(any(), any()) }
    }

    @Test
    fun createResponseFromAuthCode_withIdTokenToo() {
        val code = code.copy(scope = setOf("openid"))

        every { tokenRepository.existsById(any()) } returns false
        every { tokenFactory.accessFromCode(any(), code) } returns token
        every { tokenFactory.refreshFromCode(any(), code) } returns token.copy(type = TokenType.REFRESH)
        every { tokenRepository.save(any()) } answers { firstArg() }
        every { tokenFactory.responseJwtFromTokens(any(), any()) } returns response
        every { userService.getUserById(user.id) } returns user
        every { idTokenHandler.createToken(user, code) } returns "id_token"

        response.idToken = "id_token"
        Assertions.assertEquals(response, service.createResponseFromAuthCode(code))

        verify { tokenFactory.accessFromCode(any(), code) }
        verify { tokenFactory.refreshFromCode(any(), code) }
        verify(exactly = 2) { tokenRepository.save(any()) }
        verify { tokenFactory.responseJwtFromTokens(any(), any()) }
        verify { idTokenHandler.createToken(user, code) }
    }

    /** Test function: createResponseFromRefreshToken() */

    @Test
    fun createResponseFromRefreshToken_withValidValues() {
        val refresh = token.copy(type = TokenType.REFRESH)

        every { tokenRepository.existsById(any()) } returns false
        every { tokenFactory.accessFromRefresh(any(), refresh) } returns token
        every { tokenRepository.save(any()) } answers { firstArg() }
        every { tokenFactory.responseJwtFromTokens(token, refresh) } returns response

        Assertions.assertEquals(response, service.createResponseFromRefreshToken(refresh))

        verify { tokenFactory.accessFromRefresh(any(), refresh) }
        verify { tokenRepository.save(any()) }
        verify { tokenFactory.responseJwtFromTokens(token, refresh) }
    }

    /** Test function: findOrRemoveRefreshToken() */

    @Test
    fun findOrRemoveRefreshToken_withValidValues() {
        val token = token.copy(type = TokenType.REFRESH)

        every { tokenRepository.findByValueAndType(token.value, TokenType.REFRESH) } returns token

        Assertions.assertEquals(token, service.findOrRemoveRefreshToken(token.value, token.clientId))

        verify { tokenRepository.findByValueAndType(token.value, TokenType.REFRESH) }
        verify(exactly = 0) { tokenRepository.delete(any()) }
    }

    @Test
    fun findOrRemoveRefreshToken_withNonexistentToken() {
        every { tokenRepository.findByValueAndType(token.value, TokenType.REFRESH) } returns null

        Assertions.assertEquals(null, service.findOrRemoveRefreshToken(token.value, token.clientId))

        verify { tokenRepository.findByValueAndType(token.value, TokenType.REFRESH) }
        verify(exactly = 0) { tokenRepository.delete(any()) }
    }

    @Test
    fun findOrRemoveRefreshToken_withInvalidClientId() {
        val token = token.copy(type = TokenType.REFRESH)

        every { tokenRepository.findByValueAndType(token.value, TokenType.REFRESH) } returns token
        every { tokenRepository.delete(token) } just runs

        Assertions.assertEquals(null, service.findOrRemoveRefreshToken(token.value, "invalid_id"))

        verify { tokenRepository.findByValueAndType(token.value, TokenType.REFRESH) }
        verify { tokenRepository.delete(token) }
    }

    @Test
    fun findOrRemoveRefreshToken_withExpiredToken() {
        val token = token.copy(type = TokenType.REFRESH, expiresAt = Instant.now().minusSeconds(60))

        every { tokenRepository.findByValueAndType(token.value, TokenType.REFRESH) } returns token

        Assertions.assertEquals(null, service.findOrRemoveRefreshToken(token.value, token.clientId))

        verify { tokenRepository.findByValueAndType(token.value, TokenType.REFRESH) }
        verify(exactly = 0) { tokenRepository.delete(any()) }
    }

    /** Test function: createResponseWithJustAccessToken() */

    @Test
    fun createResponseWithJustAccessToken_withValidValues() {
        every { tokenRepository.existsById(any()) } returns false
        every { tokenFactory.accessFromRawData(any(), any(), any(), any()) } returns token
        every { tokenRepository.save(any()) } answers { firstArg() }
        every { tokenFactory.responseJwtFromTokens(token, null) } returns response

        Assertions.assertEquals(response,
            service.createResponseWithJustAccessToken("client_id", "user_id", setOf("scope")))

        verify { tokenRepository.save(any()) }
        verify { tokenFactory.responseJwtFromTokens(token, null) }
    }

    /** Test function: createIntrospectResponse() */

    @Test
    fun createIntrospectResponse_withValidValues() {
        every { userService.getUserById("user_id") } returns user
        every { getIssuerString() } returns "issuer-string"

        val response = service.createIntrospectResponse(token)

        Assertions.assertEquals("true", response["active"])
        Assertions.assertEquals(token.userId, response["sub"])
        Assertions.assertEquals(token.clientId, response["client_id"])
        Assertions.assertEquals(user.username, response["username"])

        verify { userService.getUserById("user_id") }
    }

    /** Test function: revokeTokenFromString() */

    @Test
    fun revokeTokenFromString_withValidAccessToken() {
        every { accessTokenHandler.convertToValidToken(any()) } returns token
        every { tokenRepository.delete(token) } just runs

        service.revokeTokenFromString(token.value, token.clientId)

        verify(exactly = 1) { tokenRepository.delete(token) }
    }

    @Test
    fun revokeTokenFromString_withValidRefreshToken() {
        val token = token.copy(type = TokenType.REFRESH)

        every { accessTokenHandler.convertToValidToken(any()) } returns null
        every { tokenRepository.findByValueAndType(token.value, TokenType.REFRESH) } returns token
        every { tokenRepository.delete(token) } just runs

        service.revokeTokenFromString(token.value, token.clientId)

        verify(exactly = 1) { tokenRepository.delete(token) }
    }

    @Test
    fun revokeTokenFromString_withAccessTokenButInvalidClientId() {
        every { accessTokenHandler.convertToValidToken(any()) } returns token
        every { tokenRepository.delete(token) } just runs

        service.revokeTokenFromString(token.value, "invalid_id")

        verify(exactly = 0) { tokenRepository.delete(token) }
    }

    @Test
    fun revokeTokenFromString_withRefreshTokenButInvalidClientId() {
        val token = token.copy(type = TokenType.REFRESH)

        every { accessTokenHandler.convertToValidToken(any()) } returns null
        every { tokenRepository.findByValueAndType(token.value, TokenType.REFRESH) } returns token
        every { tokenRepository.delete(token) } just runs

        service.revokeTokenFromString(token.value, "invalid_id")

        verify(exactly = 0) { tokenRepository.delete(token) }
    }

    @Test
    fun revokeTokenFromString_withNonexistentToken() {
        every { accessTokenHandler.convertToValidToken(any()) } returns null
        every { tokenRepository.findByValueAndType(any(), any()) } returns null
        every { tokenRepository.delete(any()) } just runs

        service.revokeTokenFromString("nonexistent_token", "invalid_id")

        verify(exactly = 0) { tokenRepository.delete(any()) }
    }
}
