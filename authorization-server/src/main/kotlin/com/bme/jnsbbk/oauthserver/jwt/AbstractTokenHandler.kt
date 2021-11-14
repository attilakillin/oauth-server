package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import io.jsonwebtoken.*
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.util.*

/**
 * An abstract base class for implementing JWT token handling service classes.
 *
 * This class implements basic operations on generic JSON Web Tokens so that
 * implementations can focus on the token-specific properties.
 */
abstract class AbstractTokenHandler(
    private val keyRepository: RSAKeyRepository,
    private val keyPrefix: String
) {
    /** Finds or creates an RSA key pair with the given ID (and an inferred prefix). */
    private fun getKeyById(keyId: String): RSAKey {
        val id = "${keyPrefix}_${keyId}"
        return keyRepository.findByIdOrNull(id) ?: keyRepository.save(RSAKey.newWithId(id))
    }

    /** Sets the lifespan attributes of the token according to the parameter [lifespan]. */
    private fun JwtBuilder.setLifespan(lifespan: AppConfig.Lifespan): JwtBuilder {
        val now = Instant.now()
        val iat = Date.from(now)
        val nbe = Date.from(now.plusSeconds(lifespan.notBeforeOffset))
        var exp = Date.from(now.plusSeconds(lifespan.lifespan))
        if (lifespan.lifespan == 0L) exp = null

        setIssuedAt(iat)
        setNotBefore(nbe)
        setExpiration(exp)
        return this
    }

    /** Creates a signed JWT string. The last lambda parameter can be used to set unique claims. */
    protected fun createSignedToken(
        keyId: String,
        lifespan: AppConfig.Lifespan,
        setUniqueClaims: JwtBuilder.() -> Unit
    ): String {
        val header = mapOf("typ" to "JWT", "kid" to keyId, "alg" to RSAKey.algorithm)

        return Jwts.builder()
            .setHeader(header)
            .setLifespan(lifespan)
            .apply(setUniqueClaims)
            .signWith(getKeyById(keyId).private)
            .compact()
    }

    /** Validates a JWT string. The last lambda parameter can be used for additional validation. */
    protected fun validateToken(
        token: String,
        keyId: String,
        validate: (Jws<Claims>) -> Boolean
    ): Boolean {
        val jws = parseSignedToken(token, keyId) ?: return false
        val now = Date.from(Instant.now())

        return validate(jws)
            && jws.header.keyId == "${keyPrefix}_${keyId}"
            && jws.body.notBefore.before(now)
            && jws.body.expiration?.after(now) ?: true
    }

    /** Parse a signed token string with the given key ID. Returns null if the token or key is not valid. */
    protected fun parseSignedToken(token: String, keyId: String): Jws<Claims>? {
        val parser = Jwts.parserBuilder().setSigningKey(getKeyById(keyId).public).build()

        return try {
            parser.parseClaimsJws(token)
        } catch (ex: JwtException) { null }
    }

    /** Parse an unsigned token string. Returns null if the token is not a valid JWT. */
    protected fun parseUnsignedToken(token: String): Jwt<Header<*>, Claims>? {
        val parser = Jwts.parserBuilder().build()

        return try {
            parser.parseClaimsJwt(token)
        } catch (ex: JwtException) { null }
    }
}
