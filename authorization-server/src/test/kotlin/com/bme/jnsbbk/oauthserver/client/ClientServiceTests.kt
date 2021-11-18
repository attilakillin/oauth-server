package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.entities.ClientRequest
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
class ClientServiceTests {
    private val repository = mockk<ClientRepository>()
    private val service = ClientService(repository)

    private val id = "client_id"
    private val secret = "client_secret"
    private val client = Client(
        id = id,
        secret = secret,
        redirectUris = setOf(),
        tokenEndpointAuthMethod = "none",
        grantTypes = setOf(),
        responseTypes = setOf(),
        scope = setOf(),
        idIssuedAt = Instant.now(),
        secretExpiresAt = null,
        registrationAccessToken = "access_token"
    )

    private val request = ClientRequest(
        id = null,
        secret = null,
        redirectUris = setOf("redirect_uri"),
        tokenEndpointAuthMethod = "client_secret_basic",
        grantTypes = setOf("authorization_code"),
        responseTypes = setOf("code"),
        scope = setOf("scope")
    )

    /** Test function: authenticate() **/

    @Test
    fun authenticate_withValidIdAndSecret() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(client, service.authenticate(id, secret))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticate_withValidIdButInvalidSecret() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(null, service.authenticate(id, "invalid_secret"))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticate_withInvalidId() {
        every { repository.findByIdOrNull("invalid_id") } returns null
        Assertions.assertEquals(null, service.authenticate("invalid_id", secret))
        verify { repository.findByIdOrNull("invalid_id") }
    }

    /** Test function: authenticateBasic() **/

    @Test
    fun authenticateBasic_withValidIdAndSecret() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(client, service.authenticateBasic(basicAuth(id, secret)))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticateBasic_withValidIdButInvalidSecret() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(null, service.authenticateBasic(basicAuth(id, "invalid_secret")))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticateBasic_withInvalidId() {
        every { repository.findByIdOrNull("invalid_id") } returns null
        Assertions.assertEquals(null, service.authenticateBasic(basicAuth("invalid_id", secret)))
        verify { repository.findByIdOrNull("invalid_id") }
    }

    /** Test function: authenticateParam() **/

    @Test
    fun authenticateParam_withValidIdAndSecret() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(client, service.authenticateParam(paramAuth(id, secret)))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticateParam_withValidIdButInvalidSecret() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(null, service.authenticateParam(paramAuth(id, "invalid_secret")))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticateParam_withInvalidId() {
        every { repository.findByIdOrNull("invalid_id") } returns null
        Assertions.assertEquals(null, service.authenticateParam(paramAuth("invalid_id", secret)))
        verify { repository.findByIdOrNull("invalid_id") }
    }

    @Test
    fun authenticateParam_withNullParameters() {
        Assertions.assertEquals(null, service.authenticateParam(mapOf()))
    }

    /** Test function: authenticateWithEither() **/

    @Test
    fun authenticateEither_withValidBasic() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(client,
            service.authenticateWithEither(basicAuth(id, secret), mapOf()))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticateEither_withValidParams() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(client,
            service.authenticateWithEither(null, paramAuth(id, secret)))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticateEither_withBothValidBasicAndParams() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(null,
            service.authenticateWithEither(basicAuth(id, secret), paramAuth(id, secret)))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticateEither_withBothInvalidBasicAndParams() {
        every { repository.findByIdOrNull("invalid_id") } returns null
        Assertions.assertEquals(null,
            service.authenticateWithEither(basicAuth("invalid_id", secret), paramAuth("invalid_id", secret)))
        verify { repository.findByIdOrNull("invalid_id") }
    }

    /** Test function: validateClientRegistration() **/

    @Test
    fun validateClientRegistration_withValidRequest() {
        Assertions.assertTrue(service.validateClientRegistration(request))
    }

    @Test
    fun validateClientRegistration_withIdAndSecretGiven() {
        val request = request.copy(id = id, secret = secret)
        Assertions.assertFalse(service.validateClientRegistration(request))
    }

    @Test
    fun validateClientRegistration_withNoRedirectUri() {
        val request = request.copy(redirectUris = setOf())
        Assertions.assertFalse(service.validateClientRegistration(request))
    }

    @Test
    fun validateClientRegistration_withNoScope() {
        val request = request.copy(scope = setOf())
        Assertions.assertFalse(service.validateClientRegistration(request))
    }

    @Test
    fun validateClientRegistration_withInvalidGrantType() {
        val request = request.copy(grantTypes = setOf("invalid"))
        Assertions.assertFalse(service.validateClientRegistration(request))
    }

    @Test
    fun validateClientRegistration_withInvalidResponseType() {
        val request = request.copy(responseTypes = setOf("invalid"))
        Assertions.assertFalse(service.validateClientRegistration(request))
    }

    @Test
    fun validateClientRegistration_withInvalidAuthMethod() {
        val request = request.copy(tokenEndpointAuthMethod = "invalid")
        Assertions.assertFalse(service.validateClientRegistration(request))
    }

    /** Test function: validateClientUpdate() **/

    @Test
    fun validateClientUpdate_withValidRequest() {
        val request = request.copy(id = id, secret = secret)
        Assertions.assertTrue(service.validateClientUpdate(client, request))
    }

    @Test
    fun validateClientUpdate_withNoIdAndSecretGiven() {
        Assertions.assertFalse(service.validateClientUpdate(client, request))
    }

    @Test
    fun validateClientUpdate_withNoRedirectUri() {
        val request = request.copy(id = id, secret = secret, redirectUris = setOf())
        Assertions.assertFalse(service.validateClientUpdate(client, request))
    }

    @Test
    fun validateClientUpdate_withNoScope() {
        val request = request.copy(id = id, secret = secret, scope = setOf())
        Assertions.assertFalse(service.validateClientUpdate(client, request))
    }

    @Test
    fun validateClientUpdate_withInvalidGrantType() {
        val request = request.copy(id = id, secret = secret, grantTypes = setOf("invalid"))
        Assertions.assertFalse(service.validateClientUpdate(client, request))
    }

    @Test
    fun validateClientUpdate_withInvalidResponseType() {
        val request = request.copy(id = id, secret = secret, responseTypes = setOf("invalid"))
        Assertions.assertFalse(service.validateClientUpdate(client, request))
    }

    @Test
    fun validateClientUpdate_withInvalidAuthMethod() {
        val request = request.copy(id = id, secret = secret, tokenEndpointAuthMethod = "invalid")
        Assertions.assertFalse(service.validateClientUpdate(client, request))
    }

    /** Test function: createValidClient() **/

    @Test
    fun createValidClient_withValidRequest() {
        every { repository.existsById(any()) } returns false
        val result = service.createValidClient(request)

        Assertions.assertNotNull(result.id)
        Assertions.assertNotNull(result.secret)
        Assertions.assertEquals(request.redirectUris, result.redirectUris)
        Assertions.assertEquals(request.scope, result.scope)
        Assertions.assertEquals(request.grantTypes, result.grantTypes)
        Assertions.assertEquals(request.responseTypes, result.responseTypes)
        Assertions.assertEquals(request.tokenEndpointAuthMethod, result.tokenEndpointAuthMethod)
    }

    @Test
    fun createValidClient_withMissingOptionalFields() {
        every { repository.existsById(any()) } returns false
        val request = request.copy(tokenEndpointAuthMethod = null, grantTypes = null, responseTypes = null)
        val result = service.createValidClient(request)

        Assertions.assertNotNull(result.id)
        Assertions.assertNotNull(result.secret)
        Assertions.assertEquals(request.redirectUris, result.redirectUris)
        Assertions.assertEquals(request.scope, result.scope)
        Assertions.assertTrue(result.grantTypes.isNotEmpty())
        Assertions.assertTrue(result.responseTypes.isNotEmpty())
        Assertions.assertTrue(result.tokenEndpointAuthMethod.isNotEmpty())
    }

    /** Test function: updateValidClient() **/

    @Test
    fun updateValidClient_withValidRequest() {
        val request = request.copy(id = id, secret = secret)
        val result = service.updateValidClient(client, request)

        Assertions.assertEquals(id, result.id)
        Assertions.assertEquals(secret, result.secret)
        Assertions.assertEquals(request.redirectUris, result.redirectUris)
        Assertions.assertEquals(request.scope, result.scope)
        Assertions.assertEquals(request.grantTypes, result.grantTypes)
        Assertions.assertEquals(request.responseTypes, result.responseTypes)
        Assertions.assertEquals(request.tokenEndpointAuthMethod, result.tokenEndpointAuthMethod)
    }

    @Test
    fun updateValidClient_withMissingOptionalFields() {
        val request = request.copy(id = id, secret = secret,
            tokenEndpointAuthMethod = null, grantTypes = null, responseTypes = null)
        val result = service.updateValidClient(client, request)

        Assertions.assertEquals(id, result.id)
        Assertions.assertEquals(secret, result.secret)
        Assertions.assertEquals(request.redirectUris, result.redirectUris)
        Assertions.assertEquals(request.scope, result.scope)
        Assertions.assertTrue(result.grantTypes.isNotEmpty())
        Assertions.assertTrue(result.responseTypes.isNotEmpty())
        Assertions.assertTrue(result.tokenEndpointAuthMethod.isNotEmpty())
    }

    /** Test function: getAuthorizedClient() **/

    @Test
    fun getAuthorizedClient_withValidIdAndHeader() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(client, service.getAuthorizedClient(id, "Bearer access_token"))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun getAuthorizedClient_withValidIdButInvalidHeader() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(null, service.getAuthorizedClient(id, "Bearer invalid"))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun getAuthorizedClient_withValidIdButNoHeader() {
        every { repository.findByIdOrNull(id) } returns client
        Assertions.assertEquals(null, service.getAuthorizedClient(id, null))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun getAuthorizedClient_withInvalidId() {
        every { repository.findByIdOrNull("invalid_id") } returns null
        Assertions.assertEquals(null,
            service.getAuthorizedClient("invalid_id", "Bearer access_token"))
        verify { repository.findByIdOrNull("invalid_id") }
    }

    private fun basicAuth(id: String, secret: String): String {
        val auth = Base64.getUrlEncoder().encode("$id:$secret".toByteArray()).toString(Charsets.UTF_8)
        return "Basic $auth"
    }

    private fun paramAuth(id: String, secret: String): Map<String, String> {
        return mapOf("client_id" to id, "client_secret" to secret)
    }
}
