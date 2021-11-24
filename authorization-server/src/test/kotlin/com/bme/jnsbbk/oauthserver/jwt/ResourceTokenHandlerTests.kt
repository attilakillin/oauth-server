package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.utils.getIssuerString
import io.jsonwebtoken.Jwts
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull

@ExtendWith(MockKExtension::class)
class ResourceTokenHandlerTests {
    private val userService = mockk<UserService>()
    private val rsaKeyRepository = mockk<RSAKeyRepository>()
    private val appConfig = AppConfig(
        resourceServers = AppConfig.ResourceServers(
            userToken = AppConfig.Lifespan(0, 60)
        )
    )
    private val keys = RSAKey.newWithId("id")
    private val invalidKeys = RSAKey.newWithId("invalid_id")
    init {
        mockkStatic(::getIssuerString)
        every { getIssuerString() } returns "issuer"
        every { rsaKeyRepository.findByIdOrNull(any()) } returns keys
    }

    private val handler = ResourceTokenHandler(userService, appConfig, rsaKeyRepository)

    val user = User("user_id", "username", "password")
    val server = ResourceServer("id", "secret", "base_url", setOf("scope"))

    /** Test function: createToken() */

    @Test
    fun createToken_setsUniqueClaims() {
        val jwt = handler.createToken(server, user)

        val parser = Jwts.parserBuilder().setSigningKey(keys.public).build()
        val claims = parser.parseClaimsJws(jwt)

        Assertions.assertEquals("issuer", claims.body.issuer)
        Assertions.assertEquals(user.id, claims.body.subject)
        Assertions.assertEquals(server.id, claims.body.audience)
    }

    /** Test function: isTokenValid() */

    @Test
    fun isTokenValid_withValidValues() {
        every { userService.userExistsById(user.id) } returns true

        val jwt = handler.createToken(server, user)

        Assertions.assertTrue(handler.isTokenValid(jwt, server))
    }

    @Test
    fun isTokenValid_withInvalidIssuer() {
        every { userService.userExistsById(user.id) } returns true

        val jwt = handler.createToken(server, user)

        every { getIssuerString() } returns "invalid_issuer"

        Assertions.assertFalse(handler.isTokenValid(jwt, server))
    }

    @Test
    fun isTokenValid_withInvalidAudience() {
        every { userService.userExistsById(user.id) } returns true

        val jwt = handler.createToken(server, user)

        Assertions.assertFalse(handler.isTokenValid(jwt, server.copy(id = "invalid")))
    }

    @Test
    fun isTokenValid_withNonexistentUser() {
        every { userService.userExistsById(user.id) } returns false

        val jwt = handler.createToken(server, user)

        Assertions.assertFalse(handler.isTokenValid(jwt, server))
    }

    /** Test function: getUserFromToken() */

    @Test
    fun getUserFromToken_withValidToken() {
        every { userService.getUserById(user.id) } returns user

        val jwt = handler.createToken(server, user)

        Assertions.assertNotNull(handler.getUserFromToken(jwt, server))
    }

    @Test
    fun getUserFromToken_withNonexistentUser() {
        every { userService.getUserById(user.id) } returns null

        val jwt = handler.createToken(server, user)

        Assertions.assertNull(handler.getUserFromToken(jwt, server))
    }

    @Test
    fun getUserFromToken_withInvalidServer() {
        val jwt = handler.createToken(server, user)

        every { rsaKeyRepository.findByIdOrNull(any()) } returns invalidKeys
        Assertions.assertNull(handler.getUserFromToken(jwt, server.copy(id = "invalid_id")))
    }
}
