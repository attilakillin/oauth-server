package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import io.jsonwebtoken.Jwts
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class IdTokenJwtHandler(
    private val appConfig: AppConfig,
    rsaKeyRepository: RSAKeyRepository
) {
    private val id = "openid_idtoken"
    private val keyPair: RSAKey = rsaKeyRepository.findByIdOrNull(id)
        ?: rsaKeyRepository.save(RSAKey.newWithId(id))
    
    /** Creates a signed JWT with the resource server id as the audience and the user id as the subject. */
    fun createSigned(clientId: String, user: User, scope: Set<String>, nonce: String?): String {
        val lifespan = appConfig.tokens.idToken
        val now = Instant.now()

        val additionalClaims = mutableMapOf<String, Any>()
        if (nonce != null) additionalClaims["nonce"] = nonce

        if ("profile" in scope) additionalClaims["name"] = user.info.name
        if ("email" in scope) additionalClaims["email"] = user.info.email
        if ("address" in scope) additionalClaims["address"] = user.info.address

        return Jwts.builder()
            .setHeader(mapOf("typ" to "JWT", "alg" to RSAKey.algorithm, "kid" to id))
            .setIssuer(getServerBaseUrl())
            .setSubject(user.id)
            .setAudience(clientId)
            .setIssuedAt(Date.from(now))
            .setNotBefore(Date.from(now.plusSeconds(lifespan.notBeforeOffset)))
            .setExpiration(Date.from(now.plusSeconds(lifespan.lifespan)))
            .addClaims(additionalClaims)
            .signWith(keyPair.private)
            .compact()
    }
}
