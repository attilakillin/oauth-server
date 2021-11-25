package com.bme.jnsbbk.oauthserver.resource

import com.bme.jnsbbk.oauthserver.jwt.ResourceTokenHandler
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.util.*

@ExtendWith(MockKExtension::class)
class ResourceServerServiceTests {
    private val repository = mockk<ResourceServerRepository>()
    private val jwtHandler = mockk<ResourceTokenHandler>()
    private val service = ResourceServerService(repository, jwtHandler)

    private val id = "server_id"
    private val secret = "server_secret"
    private val server = ResourceServer(id, secret, "base_url", setOf("scope"))

    /** Test function: authenticate() **/

    @Test
    fun authenticate_withValidIdAndSecret() {
        every { repository.findByIdOrNull(id) } returns server
        Assertions.assertEquals(server, service.authenticate(id, secret))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticate_withValidIdButInvalidSecret() {
        every { repository.findByIdOrNull(id) } returns server
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
        every { repository.findByIdOrNull(id) } returns server
        Assertions.assertEquals(server, service.authenticateBasic(basicAuth(id, secret)))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticateBasic_withValidIdButInvalidSecret() {
        every { repository.findByIdOrNull(id) } returns server
        Assertions.assertEquals(null, service.authenticateBasic(basicAuth(id, "invalid_secret")))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun authenticateBasic_withInvalidId() {
        every { repository.findByIdOrNull("invalid_id") } returns null
        Assertions.assertEquals(null, service.authenticateBasic(basicAuth("invalid_id", secret)))
        verify { repository.findByIdOrNull("invalid_id") }
    }

    /** Test function: serverExistsByUrl() **/

    @Test
    fun serverExistsByUrl_whenItExists() {
        every { repository.findByBaseUrl(server.baseUrl) } returns server
        Assertions.assertTrue(service.serverExistsByUrl(server.baseUrl))
        verify { repository.findByBaseUrl(server.baseUrl) }
    }

    @Test
    fun serverExistsByUrl_whenItDoesNotExist() {
        every { repository.findByBaseUrl(server.baseUrl) } returns null
        Assertions.assertFalse(service.serverExistsByUrl(server.baseUrl))
        verify { repository.findByBaseUrl(server.baseUrl) }
    }

    /** Test function: getServerById() **/

    @Test
    fun getServerById_whenItExists() {
        every { repository.findByIdOrNull(id) } returns server
        Assertions.assertEquals(server, service.getServerById(id))
        verify { repository.findByIdOrNull(id) }
    }

    @Test
    fun getServerById_whenItDoesNotExist() {
        every { repository.findByIdOrNull("invalid_id") } returns null
        Assertions.assertEquals(null, service.getServerById("invalid_id"))
        verify { repository.findByIdOrNull("invalid_id") }
    }

    /** Test function: createServer() **/

    @Test
    fun createServer_withValues() {
        every { repository.existsById(any()) } returns false
        every { repository.save(any()) } returnsArgument 0

        val result = service.createServer(server.baseUrl, server.scope)
        Assertions.assertEquals(server.baseUrl, result.baseUrl)
        Assertions.assertEquals(server.scope, result.scope)

        verify { repository.save(any()) }
    }

    /** Test function: createEncodedUserToken() **/

    @Test
    fun createEncodedUserToken_withValue() {
        val token = "token_value"
        every { jwtHandler.createToken(any(), any()) } returns token

        val result = service.createEncodedUserToken(server, mockk())
        val content = Base64.getUrlDecoder().decode(result).toString(Charsets.UTF_8)

        Assertions.assertEquals(token, content)

        verify { jwtHandler.createToken(any(), any()) }
    }

    /** Test function: isUserTokenValid() */

    @Test
    fun isUserTokenValid_worksAsExpected() {
        every { jwtHandler.isTokenValid(any(), any()) } returns true
        Assertions.assertTrue(service.isUserTokenValid(server, "token"))

        every { jwtHandler.isTokenValid(any(), any()) } returns false
        Assertions.assertFalse(service.isUserTokenValid(server, "token"))
    }

    /** Test function: getUserFromUserToken() */

    @Test
    fun getUserFromUserToken_worksAsExpected() {
        every { jwtHandler.getUserFromToken(any(), any()) } returns mockk()
        Assertions.assertNotNull(service.getUserFromUserToken(server, "token"))

        every { jwtHandler.getUserFromToken(any(), any()) } returns null
        Assertions.assertNull(service.getUserFromUserToken(server, "token"))
    }

    private fun basicAuth(id: String, secret: String): String {
        val auth = Base64.getUrlEncoder().encode("$id:$secret".toByteArray()).toString(Charsets.UTF_8)
        return "Basic $auth"
    }
}
