package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.exceptions.BadRequestException
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*

/**
 * JWT handler class for every [Token] related function.
 *
 * Handles JWT creation and validation for access and refresh tokens. These tokens are
 * signed with a private RSA key, and can be validated with the respective public key.
 */
@Service
class TokenJwtHandler(
    private val rsaKeyRepository: RSAKeyRepository,
    private val clientRepository: ClientRepository
) {
    /** Creates a signed JWT token from the specified access [token]. */
    fun createSignedAccess(token: Token): String {
        val client = getClientById(token.clientId)
        return Jwts.builder()
            .setHeader(mapOf("typ" to "JWT", "alg" to RSAKey.algorithm, "kid" to "token_" + client.id))
            .setInfo(token, client)
            .signWith(getKeyById(client.id).private)
            .compact()
    }

    /** Checks whether the given JSON Web Token represents an active token. */
    fun getValidTokenId(jwt: String): String? {
        val unsecureJwt = jwt.replaceAfterLast('.', "")
        val jwtObject = Jwts.parserBuilder().build().parseClaimsJwt(unsecureJwt)
        if (jwtObject.header["kid"] != "token_" + jwtObject.body.audience) return null
        val key = getKeyById(jwtObject.body.audience).public

        return parseClaimsOrNull(jwt, key)?.id
    }

    /** Returns an RSA key if it exists for the given [id], or creates one if it doesn't. */
    private fun getKeyById(id: String): RSAKey {
        val keyId = "token_$id"
        return rsaKeyRepository.findByIdOrNull(keyId)
            ?: rsaKeyRepository.save(RSAKey.newWithId(keyId))
    }

    /** Either returns a valid client with the given [id], or throws a [BadRequestException]. */
    private fun getClientById(id: String): Client =
        clientRepository.findByIdOrNull(id) ?: badRequest("invalid_client")

    /** Extension function that sets every necessary claim on the receiver JWT. */
    private fun JwtBuilder.setInfo(token: Token, client: Client): JwtBuilder {
        setIssuer(getServerBaseUrl())
        setSubject(token.userId)
        setAudience(client.id)
        setId(token.value)
        setIssuedAt(Date.from(token.issuedAt))
        if (token.expiresAt != null) setExpiration(Date.from(token.expiresAt))
        return this
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
