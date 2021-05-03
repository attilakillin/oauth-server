package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.utils.RandomString
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class BasicAuthValidatorTests {
    @MockK private lateinit var repository: ClientRepository
    @InjectMockKs private var validator = BasicAuthValidator()

    private lateinit var client: Client

    @BeforeEach
    fun createSampleRequest() {
        client = Client(RandomString.generate())
        client.redirectUris = setOf("http://localhost:8082/callback")
        client.scope = setOf("alpha", "beta", "gamma", "delta")
        client.responseTypes = setOf("code")
    }

    @Test
    fun validateSensitiveOrError_failsOnEmptyRequest() {
        val request = UnvalidatedAuthRequest(null, null, null, null, null)
        assertNotNull(validator.validateSensitiveOrError(request))
    }

    @Test
    fun validateSensitiveOrError_acceptsValidRequest() {
        every { repository.findById(any()) } returns Optional.of(client)

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assertNull(validator.validateSensitiveOrError(request))
    }

    @Test
    fun validateSensitiveOrError_failsOnInvalidClient() {
        every { repository.findById(any()) } returns Optional.empty()

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assertNotNull(validator.validateSensitiveOrError(request))
    }

    @Test
    fun validateSensitiveOrError_failsOnInvalidRedirectUri() {
        every { repository.findById(any()) } returns Optional.of(client)

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = "malicious url",
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assertNotNull(validator.validateSensitiveOrError(request))
    }

    @Test
    fun validateSensitiveOrError_acceptsEmptyRedirectUri_ifClientOnlyHasOne() {
        every { repository.findById(any()) } returns Optional.of(client)

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = null,
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assertNull(validator.validateSensitiveOrError(request))
    }

    @Test
    fun validateSensitiveOrError_failsOnEmptyRedirectUri_ifClientHasMultiple() {
        every { repository.findById(any()) } returns Optional.of(client)

        client.redirectUris = setOf("http://localhost:8082/callback", "http://localhost:8083/callback")
        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = null,
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assertNotNull(validator.validateSensitiveOrError(request))
    }

    @Test
    fun validateSensitiveOrError_fixesEmptyRedirectUri() {
        every { repository.findById(any()) } returns Optional.of(client)

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = null,
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        validator.validateSensitiveOrError(request)
        assertEquals(client.redirectUris.first(), request.redirectUri)
    }

    @Test
    fun validateAdditionalOrError_throwsOnEmptyRequest() {
        val request = UnvalidatedAuthRequest(null, null, null, null, null)
        assertThrows<Exception> { validator.validateAdditionalOrError(request) }
    }

    @Test
    fun validateAdditionalOrError_throwsOnInvalidClient() {
        every { repository.findById(any()) } returns Optional.empty()

        val request = UnvalidatedAuthRequest("invalid", null, null, null, null)
        assertThrows<Exception> { validator.validateAdditionalOrError(request) }
    }

    @Test
    fun validateAdditionalOrError_failsOnInvalidResponseType() {
        every { repository.findById(any()) } returns Optional.of(client)

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = "invalid",
            state = null,
            scope = client.scope
        )
        assertNotNull(validator.validateAdditionalOrError(request))
    }

    @Test
    fun validateAdditionalOrError_failsOnInvalidScope() {
        every { repository.findById(any()) } returns Optional.of(client)

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = client.responseTypes.first(),
            state = null,
            scope = setOf("malicious", "scope_elements")
        )
        assertNotNull(validator.validateAdditionalOrError(request))
    }

    @Test
    fun validateAdditionalOrError_acceptsValidRequest() {
        every { repository.findById(any()) } returns Optional.of(client)

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assertNull(validator.validateAdditionalOrError(request))
    }

    @Test
    fun validateAdditionalOrError_fixesEmptyScope() {
        every { repository.findById(any()) } returns Optional.of(client)

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = client.responseTypes.first(),
            state = null,
            scope = null
        )
        validator.validateAdditionalOrError(request)
        assertFalse(request.scope.isNullOrEmpty())
    }

    @Test
    fun convertToValidRequest_acceptsRequestAfterValidation() {
        every { repository.findById(any()) } returns Optional.of(client)

        val request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = null,
            responseType = client.responseTypes.first(),
            state = null,
            scope = null
        )
        validator.validateSensitiveOrError(request)
        validator.validateAdditionalOrError(request)
        assertDoesNotThrow { validator.convertToValidRequest(request) }
    }
}
