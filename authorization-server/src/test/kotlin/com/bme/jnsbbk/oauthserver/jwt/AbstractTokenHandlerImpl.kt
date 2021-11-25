package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import io.jsonwebtoken.*

/**
 * Concrete implementation of [AbstractTokenHandler].
 *
 * Directly calls all protected members of its superclass, and as such,
 * it can be used to test the behaviour of the abstract superclass.
 */
class AbstractTokenHandlerImpl(
    keyRepository: RSAKeyRepository,
    keyPrefix: String
) : AbstractTokenHandler(keyRepository, keyPrefix) {

    fun createSignedTokenImpl(
        keyId: String,
        lifespan: AppConfig.Lifespan,
        setUniqueClaims: JwtBuilder.() -> Unit
    ): String = createSignedToken(keyId, lifespan, setUniqueClaims)

    fun validateTokenImpl(
        token: String,
        keyId: String,
        validate: (Jws<Claims>) -> Boolean
    ): Boolean = validateToken(token, keyId, validate)

    fun parseSignedTokenImpl(
        token: String,
        keyId: String
    ): Jws<Claims>? = parseSignedToken(token, keyId)

    fun parseUnsignedTokenImpl(
        token: String
    ): Jwt<Header<*>, Claims>? = parseUnsignedToken(token)
}
