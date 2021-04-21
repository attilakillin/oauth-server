package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.exceptions.BadRequestException
import com.bme.jnsbbk.oauthserver.token.Token
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtHandler(
    private val rsaKeyRepository: RSAKeyRepository,
    private val clientRepository: ClientRepository
) {

    fun createUnsigned(token: Token): JwtBuilder {
        val client = getClient(token)
        return Jwts.builder()
            .setHeader(mapOf("typ" to "JWT", "alg" to "none"))
            .setInfo(token, client)
    }

    fun createSigned(token: Token): JwtBuilder {
        val client = getClient(token)
        return Jwts.builder()
            .setHeader(mapOf("typ" to "JWT", "alg" to RSAKey.algorithm))
            .setInfo(token, client)
            .signWith(getKeyForClientId(client.id!!).private)
    }

    private fun getKeyForClientId(id: String): RSAKey =
        rsaKeyRepository.findById(id).getOrNull() ?: rsaKeyRepository.save(RSAKey.newWithId(id))

    private fun getClient(token: Token): Client =
        clientRepository.findById(token.clientId).getOrNull() ?:
            throw BadRequestException("invalid_client")

    private fun JwtBuilder.setInfo(token: Token, client: Client): JwtBuilder {
        setIssuer("http://localhost:8080/") //TODO Don't hardcode server ID
        setSubject("") // TODO Set up user authentication
        setAudience(client.id)
        setIssuedAt(Date.from(token.issuedAt))
        setExpiration(Date.from(token.expiresAt))

        return this
    }
}