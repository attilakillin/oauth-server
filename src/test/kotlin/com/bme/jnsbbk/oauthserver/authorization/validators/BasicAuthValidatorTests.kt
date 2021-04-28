package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.*

@ExtendWith(MockitoExtension::class)
class BasicAuthValidatorTests {
    @Mock private lateinit var repository: ClientRepository
    @InjectMocks private val validator = BasicAuthValidator()

    companion object {
        @BeforeAll
        fun initialize() {
            MockitoAnnotations.initMocks(this)
        }
    }

    private lateinit var client: Client
    private lateinit var request: UnvalidatedAuthRequest

    @BeforeEach
    fun createSampleRequest() {
        client = Client(RandomString.generate())
        client.redirectUris = setOf("http://localhost:8082/callback")
        client.scope = setOf("alpha", "beta", "gamma", "delta")
        client.responseTypes = setOf("code")
    }

    @Test
    fun validateSensitiveOrError_failsOnEmptyRequest() {
        request = UnvalidatedAuthRequest(null, null, null, null, null)
        assert(validator.validateSensitiveOrError(request) != null)
    }

    @Test
    fun validateSensitiveOrError_acceptsValidRequest() {
        whenever(repository.findById(any())).thenReturn(Optional.of(client))
        request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assert(validator.validateSensitiveOrError(request) == null)
    }

    @Test
    fun validateSensitiveOrError_failsOnInvalidClient() {
        whenever(repository.findById(any())).thenReturn(Optional.empty())
        request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assert(validator.validateSensitiveOrError(request) != null)
    }

    @Test
    fun validateSensitiveOrError_failsOnInvalidRedirectUri() {
        whenever(repository.findById(any())).thenReturn(Optional.of(client))
        request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = "malicious url",
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assert(validator.validateSensitiveOrError(request) != null)
    }

    @Test
    fun validateSensitiveOrError_acceptsEmptyRedirectUri_ifClientOnlyHasOne() {
        whenever(repository.findById(any())).thenReturn(Optional.of(client))
        request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = null,
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assert(validator.validateSensitiveOrError(request) == null)
    }

    @Test
    fun validateSensitiveOrError_failsOnEmptyRedirectUri_ifClientHasMultiple() {
        client.redirectUris = setOf("http://localhost:8082/callback", "http://localhost:8083/callback")
        whenever(repository.findById(any())).thenReturn(Optional.of(client))
        request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = null,
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assert(validator.validateSensitiveOrError(request) != null)
    }

    @Test
    fun validateSensitiveOrError_fixesEmptyRedirectUri() {
        whenever(repository.findById(any())).thenReturn(Optional.of(client))
        request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = null,
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        validator.validateSensitiveOrError(request)
        assert(request.redirectUri == client.redirectUris.first())
    }

    @Test
    fun validateAdditionalOrError_throwsOnEmptyRequest() {
        request = UnvalidatedAuthRequest(null, null, null, null, null)
        assertThrows<Exception> { validator.validateAdditionalOrError(request) }
    }

    @Test
    fun validateAdditionalOrError_throwsOnInvalidClient() {
        whenever(repository.findById(any())).thenReturn(Optional.empty())
        request = UnvalidatedAuthRequest("invalid", null, null, null, null)
        assertThrows<Exception> { validator.validateAdditionalOrError(request) }
    }

    @Test
    fun validateAdditionalOrError_failsOnInvalidResponseType() {
        whenever(repository.findById(any())).thenReturn(Optional.of(client))
        request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = "invalid",
            state = null,
            scope = client.scope
        )
        assert(validator.validateAdditionalOrError(request) != null)
    }

    @Test
    fun validateAdditionalOrError_failsOnInvalidScope() {
        whenever(repository.findById(any())).thenReturn(Optional.of(client))
        request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = client.responseTypes.first(),
            state = null,
            scope = setOf("malicious", "scope_elements")
        )
        assert(validator.validateAdditionalOrError(request) != null)
    }

    @Test
    fun validateAdditionalOrError_acceptsValidRequest() {
        whenever(repository.findById(any())).thenReturn(Optional.of(client))
        request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = client.responseTypes.first(),
            state = null,
            scope = client.scope
        )
        assert(validator.validateAdditionalOrError(request) == null)
    }

    @Test
    fun validateAdditionalOrError_fixesEmptyScope() {
        whenever(repository.findById(any())).thenReturn(Optional.of(client))
        request = UnvalidatedAuthRequest(
            clientId = client.id,
            redirectUri = client.redirectUris.first(),
            responseType = client.responseTypes.first(),
            state = null,
            scope = null
        )
        validator.validateAdditionalOrError(request)
        assert(!request.scope.isNullOrEmpty())
    }

    @Test
    fun convertToValidRequest_acceptsRequestAfterValidation() {
        whenever(repository.findById(any())).thenReturn(Optional.of(client))
        request = UnvalidatedAuthRequest(
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