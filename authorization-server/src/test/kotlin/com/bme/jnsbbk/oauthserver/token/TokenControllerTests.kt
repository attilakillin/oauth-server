package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.AuthCodeRepository
import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.client.ClientService
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.resource.ResourceServerService
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenResponse
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.*

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TokenController::class)
class TokenControllerTests {
    @Autowired private lateinit var mockMvc: MockMvc

    @MockkBean private lateinit var clientService: ClientService
    @MockkBean private lateinit var resourceServerService: ResourceServerService
    @MockkBean private lateinit var authCodeRepository: AuthCodeRepository
    @MockkBean private lateinit var tokenRepository: TokenRepository
    @MockkBean private lateinit var tokenFactory: TokenFactory
    @MockkBean private lateinit var passwordEncoder: PasswordEncoder
    @MockkBean private lateinit var userService: UserService

    private val client = Client(RandomString.generate())
    private val code = AuthCode(
        value = RandomString.generate(),
        clientId = client.id,
        userId = "user id",
        scope = setOf(),
        issuedAt = Instant.now(),
        notBefore = Instant.now(),
        expiresAt = null
    )

    @Test
    fun issueToken_requiresClientAuthentication() {
        every { clientService.authenticateWithEither(any(), any()) } returns null

        mockMvc
            .perform(post("/oauth/token"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun issueToken_badRequestOnUnsupportedGrantType() {
        every { clientService.authenticateWithEither(any(), any()) } returns client

        mockMvc
            .perform(post("/oauth/token").param("grant_type", "something_invalid"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun issueToken_handleAuthCode_badRequestOnNoCode() {
        every { clientService.authenticateWithEither(any(), any()) } returns client

        mockMvc
            .perform(post("/oauth/token").param("grant_type", "authorization_code"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun issueToken_handleAuthCode_badRequestOnInvalidCode() {
        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { authCodeRepository.findById(any()) } returns Optional.empty()

        mockMvc
            .perform(
                post("/oauth/token")
                    .param("grant_type", "authorization_code")
                    .param("code", "something_invalid")
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun issueToken_handleAuthCode_badRequestOnWrongCode() {
        val invalidCode = AuthCode(
            value = RandomString.generate(),
            clientId = "invalid client id",
            userId = "user id",
            scope = setOf(),
            issuedAt = Instant.now(),
            notBefore = Instant.now(),
            expiresAt = null
        )

        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { authCodeRepository.findById(any()) } returns Optional.of(invalidCode)
        every { authCodeRepository.delete(any()) } just runs

        mockMvc
            .perform(
                post("/oauth/token")
                    .param("grant_type", "authorization_code")
                    .param("code", invalidCode.value)
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun issueToken_handleAuthCode_returnsResponseWhenValid() {
        val access = Token(
            value = RandomString.generate(),
            type = TokenType.ACCESS,
            clientId = client.id,
            userId = "user id",
            scope = setOf(),
            issuedAt = Instant.now(),
            notBefore = Instant.now(),
            expiresAt = null
        )
        val refresh = access.copy(type = TokenType.REFRESH)
        val response = TokenResponse(
            accessToken = access.value,
            refreshToken = refresh.value,
            tokenType = "Bearer",
            expiresIn = null,
            scope = setOf()
        )

        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { authCodeRepository.findById(any()) } returns Optional.of(code)
        every { authCodeRepository.delete(any()) } just runs
        every { tokenRepository.save(any()) } answers { firstArg() }
        every { tokenFactory.accessFromCode(any(), any()) } returns access
        every { tokenFactory.refreshFromCode(any(), any()) } returns refresh
        every { tokenFactory.responseJwtFromTokens(any(), any()) } returns response

        mockMvc
            .perform(
                post("/oauth/token")
                    .param("grant_type", "authorization_code")
                    .param("code", code.value)
            )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(access.value)))
    }

    @Test
    fun issueToken_handleRefreshToken_badRequestOnNoToken() {
        every { clientService.authenticateWithEither(any(), any()) } returns client

        mockMvc
            .perform(post("/oauth/token").param("grant_type", "refresh_token"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun issueToken_handleRefreshToken_badRequestOnInvalidToken() {
        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { tokenRepository.findRefreshById(any()) } returns null

        mockMvc
            .perform(
                post("/oauth/token")
                    .param("grant_type", "refresh_token")
                    .param("refresh_token", "something_invalid")
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun issueToken_handleRefreshToken_badRequestOnWrongToken() {
        val invalidToken = Token(
            value = RandomString.generate(),
            type = TokenType.REFRESH,
            clientId = "invalid client id",
            userId = "user id",
            scope = setOf(),
            issuedAt = Instant.now(),
            notBefore = Instant.now(),
            expiresAt = null
        )

        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { tokenRepository.findRefreshById(any()) } returns invalidToken
        every { tokenRepository.delete(any()) } just runs

        mockMvc
            .perform(
                post("/oauth/token")
                    .param("grant_type", "refresh_token")
                    .param("refresh_token", invalidToken.value)
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun issueToken_handleRefreshToken_returnsResponseWhenValid() {
        val access = Token(
            value = RandomString.generate(),
            type = TokenType.ACCESS,
            clientId = client.id,
            userId = "user id",
            scope = setOf(),
            issuedAt = Instant.now(),
            notBefore = Instant.now(),
            expiresAt = null
        )
        val refresh = access.copy(type = TokenType.REFRESH)
        val response = TokenResponse(
            accessToken = access.value,
            refreshToken = refresh.value,
            tokenType = "Bearer",
            expiresIn = null,
            scope = setOf()
        )

        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { tokenRepository.findRefreshById(any()) } returns refresh
        every { tokenFactory.accessFromRefresh(any(), any()) } returns access
        every { tokenRepository.save(any()) } answers { firstArg() }
        every { tokenFactory.responseJwtFromTokens(any(), any()) } returns response

        mockMvc
            .perform(
                post("/oauth/token")
                    .param("grant_type", "refresh_token")
                    .param("refresh_token", refresh.value)
            )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(access.value)))
    }
}
