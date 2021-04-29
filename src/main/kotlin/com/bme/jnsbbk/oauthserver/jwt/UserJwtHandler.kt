package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.UserConfig
import com.bme.jnsbbk.oauthserver.users.entities.User
import com.bme.jnsbbk.oauthserver.users.UserRepository
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.MacProvider
import org.springframework.stereotype.Service
import java.security.Key
import java.time.Instant
import java.util.*
import javax.crypto.spec.SecretKeySpec

/**
 * JWT handler class for every [User] related function.
 *
 * Handles JWT creation and validation for user authentication tokens. As these tokens
 * are opaque to everyone but the authorization server, they are signed and validated
 * using a private key.
 *
 * As user authentication tokens should always have a short lifespan, the signature key
 * is not persisted anywhere, and restarting the authorization server will invalidate
 * every token previously issued.
 */
@Service
class UserJwtHandler (
    val userRepository: UserRepository,
    val userConfig: UserConfig
) {
    private val signatureKey: Key
    private val keyAlgorithm = SignatureAlgorithm.HS256

    init {
        val key = MacProvider.generateKey(keyAlgorithm).encoded
        signatureKey = SecretKeySpec(key, keyAlgorithm.jcaName)
    }

    /** Creates a signed JWT token representing the [user]. */
    fun createSigned(user: User): String {
        return Jwts.builder()
            .setHeader(mapOf("typ" to "JWT", "alg" to keyAlgorithm.jcaName))
            .setIssuer(getServerBaseUrl())
            .setSubject(user.id)
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plusSeconds(userConfig.tokenLifetime)))
            .signWith(signatureKey)
            .compact()
    }

    /** Returns whether the given token is currently valid, and represents an existing user. */
    fun isUserTokenValid(token: String): Boolean {
        val claims = parseClaimsOrNull(token) ?: return false

        return claims.issuer == getServerBaseUrl()
                && userRepository.existsById(claims.subject)
                && claims.issuedAt.before(Date.from(Instant.now()))
                && claims.expiration.after(Date.from(Instant.now()))
    }

    /** Returns the user ID stored in the token, or null, if the token is invalid. */
    fun getUserIdFrom(token: String): String? {
        val claims = parseClaimsOrNull(token) ?: return null
        return claims.subject
    }

    private fun parseClaimsOrNull(token: String): Claims? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(signatureKey).build()
                .parseClaimsJws(token).body
        } catch (ex: JwtException) {
            null
        }
    }
}