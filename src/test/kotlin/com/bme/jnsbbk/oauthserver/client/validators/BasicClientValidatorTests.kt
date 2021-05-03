package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.ValidationException
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.UnvalidatedClient
import com.bme.jnsbbk.oauthserver.onError
import com.bme.jnsbbk.oauthserver.utils.StringSetConverter
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
class BasicClientValidatorTests {
    @MockK private lateinit var repository: ClientRepository
    @InjectMockKs private var validator = BasicClientValidator()

    private lateinit var newClient: UnvalidatedClient
    private val extraData = mapOf("test" to "thing", "data" to "another thing")

    @BeforeEach
    fun createSampleClient() {
        newClient = UnvalidatedClient(
            id = null,
            secret = null,
            redirectUris = setOf("http://localhost:8082/callback"),
            tokenEndpointAuthMethod = null,
            grantTypes = null,
            responseTypes = null,
            scope = setOf("alpha", "beta", "gamma", "delta")
        )
        newClient.extraData.putAll(extraData)

        every { repository.existsById(any()) } returns false
    }

    @Test
    fun validateNewOrElse_failsOnEmptyClient() {
        val client = UnvalidatedClient(null, null, null, null, null, null, null)
        assertThrows<ValidationException> { validator.validateNewOrElse(client, ::onError) }
    }

    @Test
    fun validateNewOrElse_validatesSampleClient() {
        assertDoesNotThrow { validator.validateNewOrElse(newClient, ::onError) }
    }

    @Test
    fun validateNewOrElse_retainsValues() {
        val client = validator.validateNewOrElse(newClient, ::onError)
        assertEquals(newClient.redirectUris, client.redirectUris)
        assertEquals(newClient.scope, client.scope)
        extraData.forEach { (key, value) ->
            assertEquals(value, client.extraData[key])
        }
    }

    @Test
    fun validateNewOrElse_createsDefaults() {
        val client = validator.validateNewOrElse(newClient, ::onError)
        assertTrue(client.id.isNotEmpty())
        assertFalse(client.secret.isNullOrEmpty())
        assertTrue(client.tokenEndpointAuthMethod.isNotEmpty())
        assertTrue(client.grantTypes.isNotEmpty())
        assertTrue(client.responseTypes.isNotEmpty())
        assertTrue(client.idIssuedAt.isBefore(Instant.now()))
    }

    @Test
    fun validateNewOrElse_createsSensibleDefaults() {
        val client = validator.validateNewOrElse(newClient, ::onError)
        assertTrue(client.id.length >= 8)
        assertTrue(client.secret?.let { it.length > 16 } ?: false)
        assertTrue(client.tokenEndpointAuthMethod == "client_secret_basic")
        assertTrue("authorization_code" in client.grantTypes)
        assertTrue("code" in client.responseTypes)
        assertTrue(client.idIssuedAt.isBefore(Instant.now()))
    }

    @Test
    fun validateNewOrElse_disallowsSetSeparators() {
        val badScope = mutableSetOf("text" + StringSetConverter.SEPARATOR)
        badScope.addAll(newClient.scope!!)
        val client = UnvalidatedClient(
            id = null,
            secret = null,
            redirectUris = newClient.redirectUris!!,
            tokenEndpointAuthMethod = null,
            grantTypes = null,
            responseTypes = null,
            scope = badScope
        )
        assertThrows<ValidationException> { validator.validateNewOrElse(client, ::onError) }
    }

    @Test
    fun validateUpdateOrElse_validatesItself() {
        val client = validator.validateNewOrElse(newClient, ::onError)
        val update = UnvalidatedClient(
            client.id,
            client.secret,
            client.redirectUris,
            client.tokenEndpointAuthMethod,
            client.grantTypes,
            client.responseTypes,
            client.scope
        )
        assertDoesNotThrow { validator.validateUpdateOrElse(update, client, ::onError) }
    }

    @Test
    fun validateUpdateOrElse_doesNotValidateDifferent() {
        val client1 = validator.validateNewOrElse(newClient, ::onError)
        val client2 = validator.validateNewOrElse(newClient, ::onError)
        val update = UnvalidatedClient(
            client1.id,
            client1.secret,
            client1.redirectUris,
            client1.tokenEndpointAuthMethod,
            client1.grantTypes,
            client1.responseTypes,
            client1.scope
        )
        assertThrows<ValidationException> { validator.validateUpdateOrElse(update, client2, ::onError) }
    }
}
