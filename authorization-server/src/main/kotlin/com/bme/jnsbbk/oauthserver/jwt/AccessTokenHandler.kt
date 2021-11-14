package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.token.TokenRepository
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.utils.getIssuerString
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class AccessTokenHandler(
    private val tokenRepository: TokenRepository,
    appConfig: AppConfig,
    rsaKeyRepository: RSAKeyRepository
) : AbstractTokenHandler(rsaKeyRepository, "token") {
    private val lifespan = appConfig.tokens.accessToken

    /** Creates an access token JWT from the specified [token]. */
    fun createToken(token: Token): String {
        return createSignedToken(token.clientId, lifespan) {
            setIssuer(getIssuerString())
            setId(token.value)
        }
    }

    /** Checks whether the given token represents a valid access token or not. */
    fun isTokenValid(token: String): Boolean {
        val keyId = getKeyFromToken(token) ?: return false
        return validateToken(token, keyId) {
            val claims = it.body

            return@validateToken claims.issuer == getIssuerString()
                && tokenRepository.existsById(claims.id)
        }
    }

    /** Decodes a given JWT string into a valid token found in the token repository. */
    fun convertToValidToken(jwt: String): Token? {
        if (!isTokenValid(jwt)) return null

        val keyId = getKeyFromToken(jwt) ?: return null
        val jws = parseSignedToken(jwt, keyId) ?: return null

        return tokenRepository.findByIdOrNull(jws.body.id)
    }

    /** Extracts the key ID from the given (signed) token. Returns null if the extraction fails. */
    private fun getKeyFromToken(token: String): String? {
        val jwt = parseUnsignedToken(token.replaceAfterLast('.', ""))
        return (jwt?.header?.get("kid") as String?)?.removePrefix("token_")
    }
}
