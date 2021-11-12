package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.token.TokenRepository
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class AccessTokenHandler(
    private val tokenRepository: TokenRepository,
    appConfig: AppConfig,
    rsaKeyRepository: RSAKeyRepository
) : AbstractTokenHandler(rsaKeyRepository, "token") {
    private val lifespan = appConfig.tokens.accessToken

    fun createToken(token: Token): String {
        return createSignedToken(token.clientId, lifespan) {
            setIssuer(getServerBaseUrl())
            setId(token.value)
            //setSubject(token.userId)
            //setAudience(token.clientId)
        }
    }

    fun isTokenValid(token: String): Boolean {
        val keyId = getKeyFromToken(token) ?: return false
        return validateToken(token, keyId) {
            val claims = it.body

            return@validateToken claims.issuer == getServerBaseUrl()
                && tokenRepository.existsById(claims.id)
        }
    }

    fun convertToValidToken(jwt: String): Token? {
        if (!isTokenValid(jwt)) return null

        val keyId = getKeyFromToken(jwt) ?: return null
        val jws = parseSignedToken(jwt, keyId) ?: return null

        return tokenRepository.findByIdOrNull(jws.body.id)
    }

    private fun getKeyFromToken(token: String): String? {
        val jwt = parseUnsignedToken(token.replaceAfterLast('.', ""))
        return (jwt?.header?.get("kid") as String?)?.removePrefix("token_")
    }
}