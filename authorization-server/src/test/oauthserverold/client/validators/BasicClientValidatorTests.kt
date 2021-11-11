package com.bme.jnsbbk.oauthserverold.client.validators

import com.bme.jnsbbk.oauthserverold.ValidationException
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.entities.UnvalidatedClient
import com.bme.jnsbbk.oauthserver.client.validators.BasicClientValidator
import com.bme.jnsbbk.oauthserverold.onError
import com.bme.jnsbbk.oauthserver.utils.StringSetConverter
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
class BasicClientValidatorTests {
    @MockK private lateinit var repository: ClientRepository
    @InjectMockKs private var validator = BasicClientValidator()

    private lateinit var requested: UnvalidatedClient
    private val extraData = mapOf("test" to "thing", "data" to "another thing")

    @BeforeEach
    fun createSampleClient() {
        requested = UnvalidatedClient(
            id = null,
            secret = null,
            redirectUris = setOf("http://localhost:8082/callback"),
            tokenEndpointAuthMethod = null,
            grantTypes = null,
            responseTypes = null,
            scope = setOf("alpha", "beta", "gamma", "delta")
        )
        requested.extraData.putAll(extraData)

        every { repository.existsById(any()) } returns false
    }

    @Test
    fun validateNewOrElse_failsOnEmptyClient() {
        val client = UnvalidatedClient(null, null, null, null, null, null, null)
        assertThrows<ValidationException> { validator.validateNewOrElse(client, ::onError) }
    }

    @Test
    fun validateNewOrElse_validatesSampleClient() {
        assertDoesNotThrow { validator.validateNewOrElse(requested, ::onError) }
    }

    @Test
    fun validateNewOrElse_retainsValues() {
        val client = validator.validateNewOrElse(requested, ::onError)
        assertEquals(requested.redirectUris, client.redirectUris)
        assertEquals(requested.scope, client.scope)
        extraData.forEach { (key, value) ->
            assertEquals(value, client.extraData[key])
        }
    }

    @Test
    fun validateNewOrElse_createsDefaults() {
        val client = validator.validateNewOrElse(requested, ::onError)
        assertTrue(client.id.isNotEmpty())
        assertFalse(client.secret.isNullOrEmpty())
        assertTrue(client.tokenEndpointAuthMethod.isNotEmpty())
        assertTrue(client.grantTypes.isNotEmpty())
        assertTrue(client.responseTypes.isNotEmpty())
        assertTrue(client.idIssuedAt.isBefore(Instant.now()))
    }

    @Test
    fun validateNewOrElse_createsSensibleDefaults() {
        val client = validator.validateNewOrElse(requested, ::onError)
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
        badScope.addAll(requested.scope!!)
        val client = UnvalidatedClient(
            id = null,
            secret = null,
            redirectUris = requested.redirectUris!!,
            tokenEndpointAuthMethod = null,
            grantTypes = null,
            responseTypes = null,
            scope = badScope
        )
        assertThrows<ValidationException> { validator.validateNewOrElse(client, ::onError) }
    }

    @Test
    fun validateUpdateOrElse_validatesItself() {
        val client = validator.validateNewOrElse(requested, ::onError)
        val update = unvalidatedFromClient(client)
        assertDoesNotThrow { validator.validateUpdateOrElse(update, client, ::onError) }
    }

    @Test
    fun validateUpdateOrElse_doesNotValidateDifferent() {
        val client1 = validator.validateNewOrElse(requested, ::onError)
        val client2 = validator.validateNewOrElse(requested, ::onError)
        val update = unvalidatedFromClient(client1)
        assertThrows<ValidationException> { validator.validateUpdateOrElse(update, client2, ::onError) }
    }

    private fun unvalidatedFromClient(client: Client) = UnvalidatedClient(
        client.id,
        client.secret,
        client.redirectUris,
        client.tokenEndpointAuthMethod,
        client.grantTypes,
        client.responseTypes,
        client.scope
    )
}
