package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.utils.RandomString
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class AuthRequestServiceTests(
    @MockK val repository: ClientRepository
) {
    private val service = AuthRequestService(repository)

    private val client = Client(RandomString.generate())
    init { resetClientData() }

    private val nullRequest = UnvalidatedAuthRequest(null, null, null, null, null, null)
    private val request = UnvalidatedAuthRequest(
        clientId = client.id,
        redirectUri = client.redirectUris.first(),
        responseType = client.responseTypes.first(),
        state = null,
        scope = client.scope,
        nonce = null
    )

    @BeforeEach
    fun resetClientData() {
        client.redirectUris = setOf("http://localhost:8082/callback")
        client.scope = setOf("alpha", "beta", "gamma", "delta")
        client.responseTypes = setOf("code")
    }

    @Test
    fun validateSensitiveOrError_failsOnEmptyRequest() {
        Assertions.assertFalse(service.isSensitiveInfoValid(nullRequest).first)
    }

    @Test
    fun validateSensitiveOrError_acceptsValidRequest() {
        every { repository.findById(any()) } returns Optional.of(client)
        Assertions.assertFalse(service.isSensitiveInfoValid(nullRequest).first)
    }

    @Test
    fun validateSensitiveOrError_failsOnInvalidClient() {
        every { repository.findById(any()) } returns Optional.empty()
        Assertions.assertFalse(service.isSensitiveInfoValid(request).first)
    }

    @Test
    fun validateSensitiveOrError_failsOnInvalidRedirectUri() {
        every { repository.findById(any()) } returns Optional.of(client)
        Assertions.assertFalse(service.isSensitiveInfoValid(request.copy(redirectUri = "malicious url")).first)
    }

    @Test
    fun validateSensitiveOrError_acceptsEmptyRedirectUri_ifClientOnlyHasOne() {
        every { repository.findById(any()) } returns Optional.of(client)
        Assertions.assertTrue(service.isSensitiveInfoValid(request.copy(redirectUri = null)).first)
    }

    @Test
    fun validateSensitiveOrError_failsOnEmptyRedirectUri_ifClientHasMultiple() {
        every { repository.findById(any()) } returns Optional.of(client)
        client.redirectUris = setOf("http://localhost:8082/callback", "http://localhost:8083/callback")
        Assertions.assertFalse(service.isSensitiveInfoValid(request.copy(redirectUri = null)).first)
    }

    @Test
    fun validateAdditionalOrError_throwsOnEmptyRequest() {
        assertThrows<Exception> { service.isAdditionalInfoValid(nullRequest) }
    }

    @Test
    fun validateAdditionalOrError_throwsOnInvalidClient() {
        every { repository.findById(any()) } returns Optional.empty()
        assertThrows<Exception> { service.isAdditionalInfoValid(nullRequest.copy(clientId = "invalid")) }
    }

    @Test
    fun validateAdditionalOrError_failsOnInvalidResponseType() {
        every { repository.findById(any()) } returns Optional.of(client)
        Assertions.assertFalse(service.isAdditionalInfoValid(request.copy(responseType = "invalid")).first)
    }

    @Test
    fun validateAdditionalOrError_failsOnInvalidScope() {
        every { repository.findById(any()) } returns Optional.of(client)
        Assertions.assertFalse(service.isAdditionalInfoValid(request.copy(scope = setOf("malicious"))).first)
    }

    @Test
    fun validateAdditionalOrError_acceptsValidRequest() {
        every { repository.findById(any()) } returns Optional.of(client)
        Assertions.assertTrue(service.isAdditionalInfoValid(request).first)
    }

    @Test
    fun convertToValidRequest_acceptsRequestAfterValidation() {
        every { repository.findById(any()) } returns Optional.of(client)

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = null,
            responseType = client.responseTypes.first(),
            state = null,
            scope = null,
            nonce = null
        )
        Assertions.assertTrue(service.isSensitiveInfoValid(request).first)
        Assertions.assertTrue(service.isAdditionalInfoValid(request).first)
        Assertions.assertDoesNotThrow { service.convertToValidRequest(request) }
    }

    @Test
    fun convertToValidRequest_fixesEmptyAttributes() {
        every { repository.findById(any()) } returns Optional.of(client)

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = null,
            responseType = client.responseTypes.first(),
            state = null,
            scope = null,
            nonce = null
        )
        Assertions.assertTrue(service.isSensitiveInfoValid(request).first)
        Assertions.assertTrue(service.isAdditionalInfoValid(request).first)
        val valid = service.convertToValidRequest(request)

        Assertions.assertEquals(client.redirectUris.first(), valid.redirectUri)
        Assertions.assertEquals(client.scope, valid.scope)
    }
}
