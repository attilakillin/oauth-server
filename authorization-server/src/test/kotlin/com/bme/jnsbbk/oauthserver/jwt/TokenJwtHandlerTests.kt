package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.exceptions.ApiException
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import com.bme.jnsbbk.oauthserver.utils.RandomString
import io.jsonwebtoken.Jwts
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
class TokenJwtHandlerTests {
    private val rsaKeyRepository = mockk<RSAKeyRepository>()
    private val clientRepository = mockk<ClientRepository>()
    private val handler = TokenJwtHandler(rsaKeyRepository, clientRepository)

    private val client = Client(RandomString.generate())
    private val rsaKey = RSAKey.newWithId(client.id)
    private val token = Token(
        value = RandomString.generate(),
        type = TokenType.ACCESS,
        clientId = client.id,
        userId = "user id",
        scope = setOf(),
        issuedAt = Instant.now(),
        notBefore = Instant.now(),
        expiresAt = null
    )

    @BeforeEach
    fun initializeContext() {
        val request = MockHttpServletRequest()
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    }

    @Test
    fun createSignedAccess_createsValidJwt() {
        every { clientRepository.findById(any()) } returns Optional.of(client)
        every { rsaKeyRepository.findById(any()) } returns Optional.of(rsaKey)

        val jwt = handler.createSignedAccess(token)
        assertDoesNotThrow {
            Jwts.parserBuilder()
                .setSigningKey(rsaKey.public).build()
                .parseClaimsJws(jwt)
        }
    }

    @Test
    fun createSignedAccess_retainsTokenClaims() {
        every { clientRepository.findById(any()) } returns Optional.of(client)
        every { rsaKeyRepository.findById(any()) } returns Optional.of(rsaKey)

        val jwt = handler.createSignedAccess(token)
        val claims = Jwts.parserBuilder()
            .setSigningKey(rsaKey.public).build()
            .parseClaimsJws(jwt).body
        assertEquals(token.value, claims.id)
        assertEquals(token.clientId, claims.audience)
        assertEquals(token.userId, claims.subject)
    }

    @Test
    fun createSignedAccess_failsOnInvalidClient() {
        every { clientRepository.findById(any()) } returns Optional.empty()

        assertThrows<ApiException> { handler.createSignedAccess(token) }
    }
}
