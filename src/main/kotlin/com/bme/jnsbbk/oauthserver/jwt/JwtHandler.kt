package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.exceptions.BadRequestException
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.token.Token
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Service
import java.util.*

/** JWT service class, encompassing every function
 *  that's strictly necessary for JSON Web Token creation. */
@Service
class JwtHandler(
    private val rsaKeyRepository: RSAKeyRepository,
    private val clientRepository: ClientRepository
) {
    /** Creates a signed JWT token from the specified [token].
     *  Returns the final, compact version of the JWT as a [String]. */
    fun createSigned(token: Token): String {
        val client = getClientById(token.clientId)
        return Jwts.builder()
            .setHeader(mapOf("typ" to "JWT", "alg" to RSAKey.algorithm))
            .setInfo(token, client)
            .signWith(getKeyById(client.id!!).private)
            .compact()
    }

    /** Returns an [RSAKey] with the specified [id]. If an instance with this [id] existed
     *  before, the function returns that, otherwise it creates and saves a new instance. */
    private fun getKeyById(id: String): RSAKey =
        rsaKeyRepository.findById(id).getOrNull() ?: rsaKeyRepository.save(RSAKey.newWithId(id))

    /** Returns the client with the supplied [id], or throws
     *  a [BadRequestException] if no such client exists. */
    private fun getClientById(id: String): Client =
        clientRepository.findById(id).getOrNull() ?: badRequest("invalid_client")

    /** Extension function. Allows setting every relevant claim from the
     *  supplied [token] and [client]. Behaves like other builder functions. */
    private fun JwtBuilder.setInfo(token: Token, client: Client): JwtBuilder {
        setIssuer(getServerBaseUrl())
        setSubject("") // TODO Set up user authentication
        setAudience(client.id)
        setIssuedAt(Date.from(token.issuedAt))
        setExpiration(Date.from(token.expiresAt))
        return this
    }
}