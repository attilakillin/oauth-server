package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import io.jsonwebtoken.Jwts
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
class AbstractTokenHandlerTests {
    private val prefix = "prefix"
    private val id = "id"

    private val repository = mockk<RSAKeyRepository>()
    private val handler = AbstractTokenHandlerImpl(repository, prefix)

    private val keys = RSAKey.newWithId("${prefix}_${id}")
    private val invalidKeys = RSAKey.newWithId("${prefix}_invalid")
    init {
        every { repository.findByIdOrNull("${prefix}_${id}") } returns keys
        every { repository.findByIdOrNull("${prefix}_invalid") } returns invalidKeys
    }

    /** Test function: createSignedToken() */

    @Test
    fun createSignedToken_createsValidJwt() {
        val lifespan = AppConfig.Lifespan(0, 60)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        val parser = Jwts.parserBuilder().setSigningKey(keys.public).build()

        Assertions.assertDoesNotThrow {
            parser.parseClaimsJws(jwt)
        }
    }

    @Test
    fun createSignedToken_jwtContainsValidHeader() {
        val lifespan = AppConfig.Lifespan(0, 60)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        val parser = Jwts.parserBuilder().setSigningKey(keys.public).build()
        val claims = parser.parseClaimsJws(jwt)

        Assertions.assertEquals(keys.id, claims.header.keyId)
        Assertions.assertEquals("JWT", claims.header.type)
        Assertions.assertEquals(RSAKey.algorithm, claims.header.algorithm)
    }

    @Test
    fun createSignedToken_jwtContainsValidLifespan_withNonzeroExpiration() {
        val lifespan = AppConfig.Lifespan(0, 60)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        val parser = Jwts.parserBuilder().setSigningKey(keys.public).build()
        val claims = parser.parseClaimsJws(jwt)

        val now = Instant.now()
        Assertions.assertTrue(claims.body.issuedAt.before(Date.from(now)))
        Assertions.assertTrue(claims.body.notBefore.equals(claims.body.issuedAt))
        Assertions.assertTrue(claims.body.expiration.after(Date.from(now)))
    }

    @Test
    fun createSignedToken_jwtContainsValidLifespan_withZeroExpiration() {
        val lifespan = AppConfig.Lifespan(0, 0)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        val parser = Jwts.parserBuilder().setSigningKey(keys.public).build()
        val claims = parser.parseClaimsJws(jwt)

        val now = Instant.now()
        Assertions.assertTrue(claims.body.issuedAt.before(Date.from(now)))
        Assertions.assertTrue(claims.body.notBefore.equals(claims.body.issuedAt))
        Assertions.assertNull(claims.body.expiration)
    }

    @Test
    fun createSignedToken_jwtContainsAdditionalClaims() {
        val lifespan = AppConfig.Lifespan(0, 60)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {
            setIssuer("issuer")
            setAudience("audience")
            setId("jti")
            addClaims(mapOf("custom" to "claim"))
        }

        val parser = Jwts.parserBuilder().setSigningKey(keys.public).build()
        val claims = parser.parseClaimsJws(jwt)

        Assertions.assertEquals("issuer", claims.body.issuer)
        Assertions.assertEquals("audience", claims.body.audience)
        Assertions.assertEquals("jti", claims.body.id)
        Assertions.assertEquals("claim", claims.body["custom"])
    }

    /** Test function: validateToken() */

    @Test
    fun validateToken_withValidValues() {
        val lifespan = AppConfig.Lifespan(0, 60)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        Assertions.assertTrue(handler.validateTokenImpl(jwt, id) { true })
    }

    @Test
    fun validateToken_withInvalidJwt() {
        Assertions.assertFalse(handler.validateTokenImpl("not_a_jwt", id) { true })
    }

    @Test
    fun validateToken_withNotYetValidLifespan() {
        val lifespan = AppConfig.Lifespan(60, 120)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        Assertions.assertFalse(handler.validateTokenImpl(jwt, id) { true })
    }

    @Test
    fun validateToken_withCustomSucceedingValidation() {
        val lifespan = AppConfig.Lifespan(0, 60)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        Assertions.assertTrue(handler.validateTokenImpl(jwt, id) {
            it.header.keyId == "${prefix}_${id}"
        })
    }

    @Test
    fun validateToken_withCustomFailingValidation() {
        val lifespan = AppConfig.Lifespan(0, 60)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        Assertions.assertFalse(handler.validateTokenImpl(jwt, id) { false })
    }

    /** Test function: parseSignedToken() */

    @Test
    fun parseSignedToken_withValidToken() {
        val lifespan = AppConfig.Lifespan(0, 60)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        Assertions.assertNotNull(handler.parseSignedTokenImpl(jwt, id))
    }

    @Test
    fun parseSignedToken_withInvalidToken() {
        Assertions.assertNull(handler.parseSignedTokenImpl("not_a_jwt", id))
    }

    @Test
    fun parseSignedToken_withInvalidKey() {
        val lifespan = AppConfig.Lifespan(0, 60)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        Assertions.assertNull(handler.parseSignedTokenImpl(jwt, "invalid"))
    }

    /** Test function: parseUnsignedToken() */

    @Test
    fun parseUnsignedToken_withValidUnsignedToken() {
        val lifespan = AppConfig.Lifespan(0, 60)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        val unsigned = jwt.replaceAfterLast('.', "")

        Assertions.assertNotNull(handler.parseUnsignedTokenImpl(unsigned))
    }

    @Test
    fun parseUnsignedToken_withSignedToken() {
        val lifespan = AppConfig.Lifespan(0, 60)
        val jwt = handler.createSignedTokenImpl(id, lifespan) {}

        Assertions.assertNull(handler.parseUnsignedTokenImpl(jwt))
    }
}
