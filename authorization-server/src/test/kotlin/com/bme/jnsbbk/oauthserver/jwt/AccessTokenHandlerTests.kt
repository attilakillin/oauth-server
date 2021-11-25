package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.token.TokenRepository
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import com.bme.jnsbbk.oauthserver.utils.getIssuerString
import io.jsonwebtoken.Jwts
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant

@ExtendWith(MockKExtension::class)
class AccessTokenHandlerTests {
    private val tokenRepository = mockk<TokenRepository>()
    private val rsaKeyRepository = mockk<RSAKeyRepository>()
    private val appConfig = AppConfig(
        tokens = AppConfig.Tokens(
            accessToken = AppConfig.Lifespan(0, 60)
        )
    )
    private val keys = RSAKey.newWithId("id")
    init {
        mockkStatic(::getIssuerString)
        every { getIssuerString() } returns "issuer"
        every { rsaKeyRepository.findByIdOrNull(any()) } returns keys
    }

    private val handler = AccessTokenHandler(tokenRepository, appConfig, rsaKeyRepository)

    private val token = Token(
        value = "value",
        type = TokenType.ACCESS,
        clientId = "client_id",
        userId = "user_id",
        scope = setOf("scope"),
        issuedAt = Instant.now(),
        notBefore = Instant.now(),
        expiresAt = null
    )

    /** Test function: createToken() */

    @Test
    fun createToken_setsUniqueClaims() {
        val jwt = handler.createToken(token)

        val parser = Jwts.parserBuilder().setSigningKey(keys.public).build()
        val claims = parser.parseClaimsJws(jwt)

        Assertions.assertEquals("issuer", claims.body.issuer)
        Assertions.assertEquals(token.value, claims.body.id)
    }

    /** Test function: isTokenValid() */

    @Test
    fun isTokenValid_withExistingToken() {
        every { tokenRepository.existsById(any()) } returns true

        val jwt = handler.createToken(token)

        Assertions.assertTrue(handler.isTokenValid(jwt))
    }

    @Test
    fun isTokenValid_withNonexistentToken() {
        every { tokenRepository.existsById(any()) } returns false

        val jwt = handler.createToken(token)

        Assertions.assertFalse(handler.isTokenValid(jwt))
    }

    @Test
    fun isTokenValid_withWrongIssuer() {
        every { tokenRepository.existsById(any()) } returns false

        val jwt = handler.createToken(token)

        every { getIssuerString() } returns "another_issuer"

        Assertions.assertFalse(handler.isTokenValid(jwt))
    }

    /** Test function: convertToValidToken() */

    @Test
    fun convertToValidToken_withValidValues() {
        every { tokenRepository.existsById(any()) } returns true
        every { tokenRepository.findByIdOrNull(token.value) } returns token

        val jwt = handler.createToken(token)

        Assertions.assertNotNull(handler.convertToValidToken(jwt))

        verify { tokenRepository.findByIdOrNull(token.value) }
    }

    @Test
    fun convertToValidToken_withInvalidToken() {
        Assertions.assertNull(handler.convertToValidToken("not_a_jwt"))
    }
}
