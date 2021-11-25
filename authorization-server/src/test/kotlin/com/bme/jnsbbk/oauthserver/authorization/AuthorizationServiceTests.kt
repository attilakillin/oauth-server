package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.token.TokenService
import com.bme.jnsbbk.oauthserver.token.entities.TokenResponse
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant

@ExtendWith(MockKExtension::class)
class AuthorizationServiceTests {
    private val authCodeRepository = mockk<AuthCodeRepository>()
    private val clientRepository = mockk<ClientRepository>()
    private val authCodeFactory = mockk<AuthCodeFactory>()
    private val tokenService = mockk<TokenService>()

    private val service = AuthorizationService(authCodeFactory, authCodeRepository, clientRepository, tokenService)

    private val clientId = "client_id"
    private val request = UnvalidatedAuthRequest(
        clientId = clientId,
        redirectUri = "redirect_uri",
        responseType = "code",
        scope = setOf("scope"),
        state = "state",
        nonce = "nonce"
    )
    private val client = Client(
        id = clientId,
        secret = "secret",
        redirectUris = setOf("redirect_uri"),
        tokenEndpointAuthMethod = "none",
        grantTypes = setOf("authorization_code"),
        responseTypes = setOf("code"),
        scope = setOf("scope"),
        idIssuedAt = Instant.now(),
        secretExpiresAt = null,
        registrationAccessToken = "access_token"
    )

    /** Test function: validateRequestClient() **/

    @Test
    fun validateRequestClient_withValidRequest() {
        every { clientRepository.existsById(clientId) } returns true
        Assertions.assertTrue(service.validateRequestClient(request))
        verify { clientRepository.existsById(clientId) }
    }

    @Test
    fun validateRequestClient_withInvalidClientId() {
        val request = request.copy(clientId = "invalid")

        every { clientRepository.existsById("invalid") } returns false
        Assertions.assertFalse(service.validateRequestClient(request))
        verify { clientRepository.existsById("invalid") }
    }

    @Test
    fun validateRequestClient_withNullClientId() {
        val request = request.copy(clientId = null)

        Assertions.assertFalse(service.validateRequestClient(request))
    }

    /** Test function: validateRequestUri() **/

    @Test
    fun validateRequestUri_withSingleClientUriAndUriInRequest() {
        every { clientRepository.findByIdOrNull(clientId) } returns client
        Assertions.assertTrue(service.validateRequestUri(request))
        verify { clientRepository.findByIdOrNull(clientId) }
    }

    @Test
    fun validateRequestUri_withSingleClientUriAndNoUriInRequest() {
        val request = request.copy(redirectUri = null)

        every { clientRepository.findByIdOrNull(clientId) } returns client
        Assertions.assertTrue(service.validateRequestUri(request))
        verify { clientRepository.findByIdOrNull(clientId) }
    }

    @Test
    fun validateRequestUri_withMultipleClientUriAndUriInRequest() {
        val client = client.copy(redirectUris = setOf("redirect_uri", "another_uri"))

        every { clientRepository.findByIdOrNull(clientId) } returns client
        Assertions.assertTrue(service.validateRequestUri(request))
        verify { clientRepository.findByIdOrNull(clientId) }
    }

    @Test
    fun validateRequestUri_withMultipleClientUriAndNoUriInRequest() {
        val client = client.copy(redirectUris = setOf("redirect_uri", "another_uri"))
        val request = request.copy(redirectUri = null)

        every { clientRepository.findByIdOrNull(clientId) } returns client
        Assertions.assertFalse(service.validateRequestUri(request))
        verify { clientRepository.findByIdOrNull(clientId) }
    }

    /** Test function: validateRequestResponseType() **/

    @Test
    fun validateRequestResponseType_withValidType() {
        every { clientRepository.findByIdOrNull(clientId) } returns client
        Assertions.assertTrue(service.validateRequestResponseType(request))
        verify { clientRepository.findByIdOrNull(clientId) }
    }

    @Test
    fun validateRequestResponseType_withInvalidType() {
        val request = request.copy(responseType = "invalid")

        every { clientRepository.findByIdOrNull(clientId) } returns client
        Assertions.assertFalse(service.validateRequestResponseType(request))
        verify { clientRepository.findByIdOrNull(clientId) }
    }

    /** Test function: validateRequestScope() **/

    @Test
    fun validateRequestScope_withValidScope() {
        every { clientRepository.findByIdOrNull(clientId) } returns client
        Assertions.assertTrue(service.validateRequestScope(request))
        verify { clientRepository.findByIdOrNull(clientId) }
    }

    @Test
    fun validateRequestScope_withInvalidScope() {
        val request = request.copy(scope = setOf("invalid"))

        every { clientRepository.findByIdOrNull(clientId) } returns client
        Assertions.assertFalse(service.validateRequestScope(request))
        verify { clientRepository.findByIdOrNull(clientId) }
    }

    @Test
    fun validateRequestScope_withNoScope() {
        val request = request.copy(scope = null)

        every { clientRepository.findByIdOrNull(clientId) } returns client
        Assertions.assertTrue(service.validateRequestScope(request))
        verify { clientRepository.findByIdOrNull(clientId) }
    }

    /** Test function: convertToValidRequest() **/

    @Test
    fun convertToValidRequest_withAllFieldsPresent() {
        every { clientRepository.findByIdOrNull(clientId) } returns client
        val result = service.convertToValidRequest(request)

        Assertions.assertEquals(clientId, result.clientId)
        Assertions.assertEquals(request.redirectUri, result.redirectUri)
        Assertions.assertEquals(request.responseType, result.responseType)
        Assertions.assertEquals(request.scope, result.scope)
        Assertions.assertEquals(request.state, result.state)
        Assertions.assertEquals(request.nonce, result.nonce)

        verify { clientRepository.findByIdOrNull(clientId) }
    }

    @Test
    fun convertToValidRequest_withMissingOptionalFields() {
        every { clientRepository.findByIdOrNull(clientId) } returns client
        val request = request.copy(redirectUri = null, scope = null)
        val result = service.convertToValidRequest(request)

        Assertions.assertEquals(clientId, result.clientId)
        Assertions.assertEquals(client.redirectUris.first(), result.redirectUri)
        Assertions.assertEquals(request.responseType, result.responseType)
        Assertions.assertEquals(client.scope, result.scope)
        Assertions.assertEquals(request.state, result.state)
        Assertions.assertEquals(request.nonce, result.nonce)

        verify { clientRepository.findByIdOrNull(clientId) }
    }

    /** Test function: extractPrefixedScopes() **/

    @Test
    fun extractPrefixedScopes_withValidInput() {
        val scope = mapOf("scope_read" to "", "scope_write" to "", "scope_extra" to "")
        val result = service.extractPrefixedScopes(scope, "scope_")

        Assertions.assertEquals(setOf("read", "write", "extra"), result)
    }

    /** Test function: createAuthCode() **/

    @Test
    fun createAuthCode_withValidRequest() {
        val authCode = AuthCode(
            value = "value",
            clientId = clientId,
            userId = "user_id",
            scope = setOf("scope"),
            nonce = request.nonce,
            issuedAt = Instant.now(),
            notBefore = Instant.now(),
            expiresAt = null
        )

        every { authCodeRepository.existsById(any()) } returns false
        every { authCodeFactory.fromRequest(any(), any()) } returns authCode
        every { authCodeRepository.save(authCode) } returns authCode
        every { clientRepository.findByIdOrNull(clientId) } returns client

        val validRequest = service.convertToValidRequest(request)
        Assertions.assertEquals(authCode, service.createAuthCode(validRequest))

        verify { authCodeRepository.save(authCode) }
    }

    /** Test function: createImplicitResponse() **/

    @Test
    fun createImplicitResponse_withValidRequest() {
        every { clientRepository.findByIdOrNull(clientId) } returns client

        val validRequest = service.convertToValidRequest(request).apply { userId = "user_id" }
        val response = TokenResponse(
            accessToken = "access_token",
            refreshToken = null,
            tokenType = "Bearer",
            expiresIn = null,
            scope = setOf("scope")
        )

        every { tokenService.createResponseWithJustAccessToken(
            validRequest.clientId, validRequest.userId, validRequest.scope) } returns response

        val result = service.createImplicitResponse(validRequest)

        Assertions.assertEquals(response.accessToken, result["access_token"])
        Assertions.assertEquals(response.tokenType, result["token_type"])
        Assertions.assertEquals("scope", result["scope"])
        Assertions.assertEquals(validRequest.state, result["state"])
    }
}
