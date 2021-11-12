package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import io.jsonwebtoken.*
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.util.*

abstract class AbstractTokenHandler(
    private val keyRepository: RSAKeyRepository,
    private val keyPrefix: String
) {

    private fun getKeyById(keyId: String): RSAKey {
        val id = "${keyPrefix}_${keyId}"
        return keyRepository.findByIdOrNull(id) ?: keyRepository.save(RSAKey.newWithId(id))
    }

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

    protected fun createSignedToken(
        keyId: String,
        lifespan: AppConfig.Lifespan,
        setUniqueClaims: JwtBuilder.() -> JwtBuilder
    ): String {
        val id = "${keyPrefix}_${keyId}"
        val header = mapOf("typ" to "JWT", "kid" to id, "alg" to RSAKey.algorithm)

        return Jwts.builder()
            .setHeader(header)
            .setUniqueClaims()
            .setLifespan(lifespan)
            .signWith(getKeyById(id).private)
            .compact()
    }

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

    protected fun parseSignedToken(token: String, keyId: String): Jws<Claims>? {
        val id = "${keyPrefix}_${keyId}"
        val parser = Jwts.parserBuilder().setSigningKey(getKeyById(id).public).build()

        return try {
            parser.parseClaimsJws(token)
        } catch (ex: JwtException) { null }
    }
}
