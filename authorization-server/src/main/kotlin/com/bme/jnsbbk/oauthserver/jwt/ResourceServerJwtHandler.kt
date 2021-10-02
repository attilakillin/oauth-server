package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.resource.ResourceServerService
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.user.entities.User
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
 * JWT handler class for tokens issued to resource servers.
 *
 * Handles JWT creation and validation for tokens issued to resource servers. These tokens
 * contain a user (OAuth resource owner) as their subject, the resource server as the audience,
 * and are digitally signed. Using these tokens, the server can check whether the caller should
 * access whatever it is they're trying to access.
 */
@Service
class ResourceServerJwtHandler(
    private val appConfig: AppConfig,
    private val userService: UserService,
    private val resourceServerService: ResourceServerService
) {
    private val signatureKey: Key
    private val keyAlgorithm = SignatureAlgorithm.HS256

    init {
        val key = MacProvider.generateKey(keyAlgorithm).encoded
        signatureKey = SecretKeySpec(key, keyAlgorithm.jcaName)
    }

    /** Creates a signed JWT with the resource server id as the audience and the user id as the subject. */
    fun createSigned(resourceServerId: String, userId: String): String {
        val lifespan = appConfig.resourceServers.userToken
        val now = Instant.now()
        val expiration = if (lifespan.lifespan == 0L) {
            null
        } else {
            Date.from(now.plusSeconds(lifespan.lifespan))
        }

        return Jwts.builder()
            .setHeader(mapOf("typ" to "JWT", "alg" to keyAlgorithm.jcaName))
            .setIssuer(getServerBaseUrl())
            .setSubject(userId)
            .setAudience(resourceServerId)
            .setIssuedAt(Date.from(now))
            .setNotBefore(Date.from(now.plusSeconds(lifespan.notBeforeOffset)))
            .setExpiration(expiration)
            .signWith(signatureKey)
            .compact()
    }

    /** Returns whether the given token is currently valid, and belongs to the given resource server. */
    fun isTokenValid(token: String, server: ResourceServer): Boolean {
        val claims = parseClaimsOrNull(token) ?: return false

        val audience = resourceServerService.getServerById(claims.audience) ?: return false

        return claims.issuer == getServerBaseUrl() &&
                userService.userExistsById(claims.subject) &&
                server.id == audience.id &&
                claims.issuedAt.before(Date.from(Instant.now())) &&
                claims.expiration?.after(Date.from(Instant.now())) ?: true
    }

    /** Returns the user ID stored in the token, or null, if the token is invalid. */
    fun getUserFrom(token: String): User? {
        val claims = parseClaimsOrNull(token) ?: return null
        return userService.getUserById(claims.subject)
    }

    /** Tries to unwrap the given token into a set of JSON Web Token claims. Returns null if it fails. */
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
