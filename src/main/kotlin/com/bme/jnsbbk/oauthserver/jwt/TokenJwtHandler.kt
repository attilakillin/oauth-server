package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Service
import java.util.*

/**
 * JWT handler class for every [Token] related function.
 *
 * Handles JWT creation and validation for access and refresh tokens. These tokens are
 * signed with a private RSA key, and can be validated with the respective public key.
 */
@Service
class TokenJwtHandler (
    private val rsaKeyRepository: RSAKeyRepository,
    private val clientRepository: ClientRepository
) {
    /** Creates a signed JWT token from the specified access [token]. */
    fun createSignedAccess(token: Token): String {
        val client = getClientById(token.clientId)
        return Jwts.builder()
            .setHeader(mapOf("typ" to "JWT", "alg" to RSAKey.algorithm))
            .setInfo(token, client)
            .signWith(getKeyById(client.id).private)
            .compact()
    }

    private fun getKeyById(id: String): RSAKey =
        rsaKeyRepository.findById(id).getOrNull() ?: rsaKeyRepository.save(RSAKey.newWithId(id))

    private fun getClientById(id: String): Client =
        clientRepository.findById(id).getOrNull() ?: badRequest("invalid_client")

    private fun JwtBuilder.setInfo(token: Token, client: Client): JwtBuilder {
        setIssuer(getServerBaseUrl())
        setSubject(token.userId)
        setAudience(client.id)
        setId(token.value)
        setIssuedAt(Date.from(token.issuedAt))
        if (token.expiresAt != null) setExpiration(Date.from(token.expiresAt))
        return this
    }
}