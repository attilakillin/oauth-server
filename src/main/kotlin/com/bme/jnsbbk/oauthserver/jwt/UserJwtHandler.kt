package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.UserConfig
import com.bme.jnsbbk.oauthserver.users.User
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

@Service
class UserJwtHandler (
    val userRepository: UserRepository,
    val userConfig: UserConfig
) {
    private val signingKey: Key
    private val keyAlgorithm = SignatureAlgorithm.HS256

    init {
        val key = MacProvider.generateKey(keyAlgorithm).encoded
        signingKey = SecretKeySpec(key, keyAlgorithm.jcaName)
    }

    fun createSigned(user: User): String {
        return Jwts.builder()
            .setHeader(mapOf("typ" to "JWT", "alg" to keyAlgorithm.jcaName))
            .setIssuer(getServerBaseUrl())
            .setSubject(user.id)
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plusSeconds(userConfig.tokenLifetime)))
            .signWith(signingKey)
            .compact()
    }

    fun isUserTokenValid(token: String): Boolean {
        val claims = parseClaimsOrNull(token) ?: return false

        return claims.issuer == getServerBaseUrl()
                && userRepository.existsById(claims.subject)
                && claims.issuedAt.before(Date.from(Instant.now()))
                && claims.expiration.after(Date.from(Instant.now()))
    }

    fun getUserIdFrom(token: String): String? {
        val claims = parseClaimsOrNull(token) ?: return null
        return claims.subject
    }

    private fun parseClaimsOrNull(token: String): Claims? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(signingKey).build()
                .parseClaimsJws(token).body
        } catch (ex: JwtException) {
            null
        }
    }
}