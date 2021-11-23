package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.AuthCodeRepository
import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.client.ClientService
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.resource.ResourceServerService
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenResponse
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(
    TokenController::class,
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [WebSecurityConfigurer::class])],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class TokenControllerTests {
    @Autowired private lateinit var mvc: MockMvc
    @MockkBean private lateinit var clientService: ClientService
    @MockkBean private lateinit var resourceServerService: ResourceServerService
    @MockkBean private lateinit var authCodeRepository: AuthCodeRepository
    @MockkBean private lateinit var tokenService: TokenService

    private val client = Client(
        id = "client_id",
        secret = "client_secret",
        redirectUris = setOf("client_redirect_uri"),
        tokenEndpointAuthMethod = "none",
        grantTypes = setOf("authorization_code", "refresh_token", "client_credentials"),
        responseTypes = setOf("irrelevant"),
        scope = setOf("scope"),
        idIssuedAt = Instant.now(),
        secretExpiresAt = null,
        registrationAccessToken = "access_token"
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
        clientId = client.id,
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
        clientId = client.id,
        userId = "user_id",
        scope = setOf("scope"),
        issuedAt = Instant.now(),
        notBefore = Instant.now(),
        expiresAt = null
    )

    /** Test function: issueToken() */

    @Test
    fun issueToken_withInvalidAuthorization() {
        every { clientService.authenticateWithEither(any(), any()) } returns null

        mvc
            .perform(post("/oauth/token"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun issueToken_withUnsupportedGrantType() {
        every { clientService.authenticateWithEither(any(), any()) } returns client

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "unsupported_type"))
            .andExpect(status().isBadRequest)
    }

    /** Test function: issueToken() with 'authorization_code' grant type */

    @Test
    fun issueToken_withAuthorizationCodeGrantType_andValidValues() {
        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { authCodeRepository.findByIdOrNull(code.value) } returns code
        every { authCodeRepository.delete(code) } just runs
        every { tokenService.createResponseFromAuthCode(code) } returns response

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "authorization_code")
                .param("code", code.value))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(response.accessToken)))

        verify { authCodeRepository.delete(code) }
    }

    @Test
    fun issueToken_withAuthorizationCodeGrantType_andNoCode() {
        every { clientService.authenticateWithEither(any(), any()) } returns client

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "authorization_code"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun issueToken_withAuthorizationCodeGrantType_andNonexistentCode() {
        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { authCodeRepository.findByIdOrNull(code.value) } returns null

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "authorization_code")
                .param("code", code.value))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun issueToken_withAuthorizationCodeGrantType_andInvalidClientId() {
        val code = code.copy(clientId = "invalid_id")
        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { authCodeRepository.findByIdOrNull(code.value) } returns code
        every { authCodeRepository.delete(code) } just runs

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "authorization_code")
                .param("code", code.value))
            .andExpect(status().isBadRequest)

        verify { authCodeRepository.delete(code) }
    }

    @Test
    fun issueToken_withAuthorizationCodeGrantType_andExpiredCode() {
        val code = code.copy(expiresAt = Instant.now().minusSeconds(60))
        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { authCodeRepository.findByIdOrNull(code.value) } returns code
        every { authCodeRepository.delete(code) } just runs

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "authorization_code")
                .param("code", code.value))
            .andExpect(status().isBadRequest)

        verify { authCodeRepository.delete(code) }
    }

    /** Test function: issueToken() with 'refresh_token' grant type */

    @Test
    fun issueToken_withRefreshTokenGrantType_andValidValues() {
        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { tokenService.findOrRemoveRefreshToken(refresh.value, client.id) } returns refresh
        every { tokenService.createResponseFromRefreshToken(refresh) } returns response

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "refresh_token")
                .param("refresh_token", refresh.value))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(response.accessToken)))

        verify { tokenService.findOrRemoveRefreshToken(refresh.value, client.id) }
    }

    @Test
    fun issueToken_withRefreshTokenGrantType_andNoRefreshToken() {
        every { clientService.authenticateWithEither(any(), any()) } returns client

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "refresh_token"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun issueToken_withRefreshTokenGrantType_andNonexistentRefreshToken() {
        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { tokenService.findOrRemoveRefreshToken(refresh.value, client.id) } returns null

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "refresh_token")
                .param("refresh_token", refresh.value))
            .andExpect(status().isBadRequest)
    }

    /** Test function: issueToken() with 'client_credentials' grant type */

    @Test
    fun issueToken_withClientCredentialsGrantType_andValidValues() {
        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { tokenService.createResponseWithJustAccessToken(any(), any(), any()) } returns response

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "client_credentials")
                .param("scope", client.scope.first()))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(response.accessToken)))

        verify { tokenService.createResponseWithJustAccessToken(any(), any(), any()) }
    }

    @Test
    fun issueToken_withClientCredentialsGrantType_andNoScope() {
        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { tokenService.createResponseWithJustAccessToken(any(), any(), any()) } returns response

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "client_credentials"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(response.accessToken)))

        verify { tokenService.createResponseWithJustAccessToken(any(), any(), any()) }
    }

    @Test
    fun issueToken_withClientCredentialsGrantType_andInvalidScope() {
        every { clientService.authenticateWithEither(any(), any()) } returns client

        mvc
            .perform(post("/oauth/token")
                .param("grant_type", "client_credentials")
                .param("scope", "invalid_scope"))
            .andExpect(status().isBadRequest)
    }

    /** Test function: introspectToken() */

    @Test
    fun introspectToken_withValidValues() {
        val token = refresh.copy(type = TokenType.ACCESS)

        every { resourceServerService.authenticateBasic(any()) } returns mockk()
        every { tokenService.convertFromJwt(any()) } returns token
        every { tokenService.createIntrospectResponse(token) } returns mapOf("success" to "true")

        mvc
            .perform(post("/oauth/token/introspect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""" {"token": "jwt_to_check"} """))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("success")))

        verify { tokenService.createIntrospectResponse(token) }
    }

    @Test
    fun introspectToken_withNoToken() {
        mvc
            .perform(post("/oauth/token/introspect"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun introspectToken_withInvalidAuthorization() {
        every { resourceServerService.authenticateBasic(any()) } returns null

        mvc
            .perform(post("/oauth/token/introspect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""" {"token": "jwt_to_check"} """))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun introspectToken_withInvalidJwt() {
        every { resourceServerService.authenticateBasic(any()) } returns mockk()
        every { tokenService.convertFromJwt(any()) } returns null

        mvc
            .perform(post("/oauth/token/introspect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""" {"token": "jwt_to_check"} """))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("false")))
    }

    @Test
    fun introspectToken_withExpiredToken() {
        val token = refresh.copy(type = TokenType.ACCESS, expiresAt = Instant.now().minusSeconds(60))

        every { resourceServerService.authenticateBasic(any()) } returns mockk()
        every { tokenService.convertFromJwt(any()) } returns token

        mvc
            .perform(post("/oauth/token/introspect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""" {"token": "jwt_to_check"} """))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("false")))
    }

    /** Test function: revokeToken() */

    @Test
    fun revokeToken_withValidValues() {
        every { clientService.authenticateWithEither(any(), any()) } returns client
        every { tokenService.revokeTokenFromString("jwt_to_check", client.id) } just runs

        mvc
            .perform(post("/oauth/token/revoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""" {"token": "jwt_to_check"} """))
            .andExpect(status().isOk)

        verify { tokenService.revokeTokenFromString("jwt_to_check", client.id) }
    }

    @Test
    fun revokeToken_withNoToken() {
        mvc
            .perform(post("/oauth/token/revoke"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun revokeToken_withInvalidAuthorization() {
        every { clientService.authenticateWithEither(any(), any()) } returns null

        mvc
            .perform(post("/oauth/token/revoke")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""" {"token": "jwt_to_check"} """))
            .andExpect(status().isUnauthorized)
    }
}
