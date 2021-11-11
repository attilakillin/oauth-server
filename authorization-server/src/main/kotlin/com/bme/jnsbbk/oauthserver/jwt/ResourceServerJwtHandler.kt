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
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.security.Key
import java.time.Instant
import java.util.*

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
    private val resourceServerService: ResourceServerService,
    private val rsaKeyRepository: RSAKeyRepository
) {
    private fun getKeyById(id: String): RSAKey {
        val keyId = "resource_$id"
        return rsaKeyRepository.findByIdOrNull(keyId)
            ?: rsaKeyRepository.save(RSAKey.newWithId(keyId))
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
            .setHeader(mapOf("typ" to "JWT", "alg" to RSAKey.algorithm, "kid" to "resource_$resourceServerId"))
            .setIssuer(getServerBaseUrl())
            .setSubject(userId)
            .setAudience(resourceServerId)
            .setIssuedAt(Date.from(now))
            .setNotBefore(Date.from(now.plusSeconds(lifespan.notBeforeOffset)))
            .setExpiration(expiration)
            .signWith(getKeyById(resourceServerId).private)
            .compact()
    }

    /** Returns whether the given token is currently valid, and belongs to the given resource server. */
    fun isTokenValid(token: String, server: ResourceServer): Boolean {
        val claims = parseClaimsOrNull(token, getKeyById(server.id).public) ?: return false

        val audience = resourceServerService.getServerById(claims.audience) ?: return false

        return claims.issuer == getServerBaseUrl() &&
                userService.userExistsById(claims.subject) &&
                server.id == audience.id &&
                claims.issuedAt.before(Date.from(Instant.now())) &&
                claims.expiration?.after(Date.from(Instant.now())) ?: true
    }

    /** Returns the user ID stored in the token, or null, if the token is invalid. */
    fun getUserFrom(token: String): User? {
        val unsecureJwt = token.replaceAfterLast('.', "")
        val jwtObject = Jwts.parserBuilder().build().parseClaimsJwt(unsecureJwt)
        if (jwtObject.header["kid"] != "resource_" + jwtObject.body.audience) return null
        val key = getKeyById(jwtObject.body.audience).public

        val claims = parseClaimsOrNull(token, key) ?: return null
        return userService.getUserById(claims.subject)
    }

    /** Tries to unwrap the given token into a set of JSON Web Token claims. Returns null if it fails. */
    private fun parseClaimsOrNull(token: String, key: Key): Claims? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token).body
        } catch (ex: JwtException) {
            null
        }
    }
}
