package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.UnvalidatedClient
import com.bme.jnsbbk.oauthserver.utils.StringSetConverter
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class BasicClientValidatorTests {
    @Mock
    private lateinit var repository: ClientRepository

    @InjectMocks
    private val validator = BasicClientValidator()

    private class ValidationException : Exception()
    private fun onError(): Nothing = throw ValidationException()

    private lateinit var newClient: UnvalidatedClient
    private val redirectUris = setOf("http://localhost:8082/callback")
    private val scope = setOf("alpha", "beta", "gamma", "delta")
    private val extraData = mapOf("test" to "thing", "data" to "another thing")

    companion object {
        @BeforeAll
        fun initialize() {
            MockitoAnnotations.initMocks(this)
        }
    }

    @BeforeEach
    fun createSampleClient() {
        newClient = UnvalidatedClient(null, null, redirectUris,
            null, null, null, scope)
        newClient.extraData.putAll(extraData)
    }

    @Test
    fun validateNewOrElse_failsOnEmptyClient() {
        val client = UnvalidatedClient(null, null, null,
            null, null, null, null)
        assertThrows<ValidationException> { validator.validateNewOrElse(client, ::onError) }
    }

    @Test
    fun validateNewOrElse_validatesSampleClient() {
        assertDoesNotThrow { validator.validateNewOrElse(newClient, ::onError) }
    }

    @Test
    fun validateNewOrElse_retainsValues() {
        val client = validator.validateNewOrElse(newClient, ::onError)
        assert(client.redirectUris == newClient.redirectUris)
        assert(client.scope == newClient.scope)
        extraData.forEach { (key, value) ->
            assert(client.extraData[key] == value)
        }
    }

    @Test
    fun validateNewOrElse_createsDefaults() {
        val client = validator.validateNewOrElse(newClient, ::onError)
        assert(client.id.isNotEmpty())
        assert(!client.secret.isNullOrEmpty())
        assert(client.tokenEndpointAuthMethod.isNotEmpty())
        assert(client.grantTypes.isNotEmpty())
        assert(client.responseTypes.isNotEmpty())
        assert(client.idIssuedAt.isBefore(Instant.now()))
    }

    @Test
    fun validateNewOrElse_createsSensibleDefaults() {
        val client = validator.validateNewOrElse(newClient, ::onError)
        assert(client.id.length >= 8)
        assert(client.secret?.let { it.length > 16 } ?: false)
        assert(client.tokenEndpointAuthMethod == "client_secret_basic")
        assert("authorization_code" in client.grantTypes)
        assert("code" in client.responseTypes)
        assert(client.idIssuedAt.isBefore(Instant.now()))
    }

    @Test
    fun validateNewOrElse_disallowsSetSeparators() {
        val badScope = mutableSetOf("text" + StringSetConverter.SEPARATOR)
        badScope.addAll(scope)
        val client = UnvalidatedClient(null, null, redirectUris,
            null, null, null, badScope)
        assertThrows<ValidationException> { validator.validateNewOrElse(client, ::onError) }
    }

    @Test
    fun validateUpdateOrElse_validatesItself() {
        val client = validator.validateNewOrElse(newClient, ::onError)
        val update = UnvalidatedClient(client.id, client.secret, client.redirectUris,
            client.tokenEndpointAuthMethod, client.grantTypes, client.responseTypes, client.scope)
        assertDoesNotThrow { validator.validateUpdateOrElse(update, client, ::onError) }
    }

    @Test
    fun validateUpdateOrElse_doesNotValidateDifferent() {
        val client1 = validator.validateNewOrElse(newClient, ::onError)
        val client2 = validator.validateNewOrElse(newClient, ::onError)
        val update = UnvalidatedClient(client1.id, client1.secret, client1.redirectUris,
            client1.tokenEndpointAuthMethod, client1.grantTypes, client1.responseTypes, client1.scope)
        assertThrows<ValidationException> { validator.validateUpdateOrElse(update, client2, ::onError) }
    }
}