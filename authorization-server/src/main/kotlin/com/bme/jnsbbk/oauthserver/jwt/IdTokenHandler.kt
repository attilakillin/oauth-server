package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import org.springframework.stereotype.Service

@Service
class IdTokenHandler(
    appConfig: AppConfig,
    rsaKeyRepository: RSAKeyRepository
) : AbstractTokenHandler(rsaKeyRepository, "openid") {
    private val lifespan = appConfig.tokens.idToken
    private val key = "token"

    /** Creates an ID token from the given authorization [code] representing the given [user]. */
    fun createToken(user: User, code: AuthCode): String {
        val info = mutableMapOf<String, Any>()
        if (code.nonce != null) info["nonce"] = code.nonce

        if ("profile" in code.scope) info["name"] = user.info.name
        if ("email" in code.scope) info["email"] = user.info.email
        if ("address" in code.scope) info["address"] = user.info.address

        return createSignedToken(key, lifespan) {
            setIssuer(getServerBaseUrl())
            setSubject(user.id)
            setAudience(code.clientId)
            addClaims(info)
        }
    }
}
