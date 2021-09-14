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

    private val client = Client(RandomString.generate())
    init { resetClientData() }

    private val nullRequest = UnvalidatedAuthRequest(null, null, null, null, null)
    private val request = UnvalidatedAuthRequest(
        clientId = client.id,
        redirectUri = client.redirectUris.first(),
        responseType = client.responseTypes.first(),
        state = null,
        scope = client.scope
    )

    @BeforeEach
    fun resetClientData() {
        client.redirectUris = setOf("http://localhost:8082/callback")
        client.scope = setOf("alpha", "beta", "gamma", "delta")
        client.responseTypes = setOf("code")
    }

    @Test
    fun validateSensitiveOrError_failsOnEmptyRequest() {
        assertNotNull(validator.validateSensitiveOrError(nullRequest))
    }

    @Test
    fun validateSensitiveOrError_acceptsValidRequest() {
        every { repository.findById(any()) } returns Optional.of(client)
        assertNull(validator.validateSensitiveOrError(request))
    }

    @Test
    fun validateSensitiveOrError_failsOnInvalidClient() {
        every { repository.findById(any()) } returns Optional.empty()
        assertNotNull(validator.validateSensitiveOrError(request))
    }

    @Test
    fun validateSensitiveOrError_failsOnInvalidRedirectUri() {
        every { repository.findById(any()) } returns Optional.of(client)
        assertNotNull(validator.validateSensitiveOrError(request.copy(redirectUri = "malicious url")))
    }

    @Test
    fun validateSensitiveOrError_acceptsEmptyRedirectUri_ifClientOnlyHasOne() {
        every { repository.findById(any()) } returns Optional.of(client)
        assertNull(validator.validateSensitiveOrError(request.copy(redirectUri = null)))
    }

    @Test
    fun validateSensitiveOrError_failsOnEmptyRedirectUri_ifClientHasMultiple() {
        every { repository.findById(any()) } returns Optional.of(client)
        client.redirectUris = setOf("http://localhost:8082/callback", "http://localhost:8083/callback")
        assertNotNull(validator.validateSensitiveOrError(request.copy(redirectUri = null)))
    }

    @Test
    fun validateSensitiveOrError_fixesEmptyRedirectUri() {
        every { repository.findById(any()) } returns Optional.of(client)
        validator.validateSensitiveOrError(request.copy(redirectUri = null))
        assertEquals(client.redirectUris.first(), request.redirectUri)
    }

    @Test
    fun validateAdditionalOrError_throwsOnEmptyRequest() {
        assertThrows<Exception> { validator.validateAdditionalOrError(nullRequest) }
    }

    @Test
    fun validateAdditionalOrError_throwsOnInvalidClient() {
        every { repository.findById(any()) } returns Optional.empty()
        assertThrows<Exception> { validator.validateAdditionalOrError(nullRequest.copy(clientId = "invalid")) }
    }

    @Test
    fun validateAdditionalOrError_failsOnInvalidResponseType() {
        every { repository.findById(any()) } returns Optional.of(client)
        assertNotNull(validator.validateAdditionalOrError(request.copy(responseType = "invalid")))
    }

    @Test
    fun validateAdditionalOrError_failsOnInvalidScope() {
        every { repository.findById(any()) } returns Optional.of(client)
        assertNotNull(validator.validateAdditionalOrError(request.copy(scope = setOf("malicious"))))
    }

    @Test
    fun validateAdditionalOrError_acceptsValidRequest() {
        every { repository.findById(any()) } returns Optional.of(client)
        assertNull(validator.validateAdditionalOrError(request))
    }

    @Test
    fun validateAdditionalOrError_fixesEmptyScope() {
        every { repository.findById(any()) } returns Optional.of(client)
        validator.validateAdditionalOrError(request.copy(scope = null))
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
