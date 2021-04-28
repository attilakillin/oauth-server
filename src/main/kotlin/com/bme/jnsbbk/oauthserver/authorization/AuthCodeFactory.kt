package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.config.TokenLifetimes
import org.springframework.stereotype.Service
import java.time.Instant

/** Factory class to create authorization codes from specific templates. */
@Service
class AuthCodeFactory (
    val tokenLifetimes: TokenLifetimes
) {
    /** Creates an authorization code with the given [value] and from the given [request]. */
    fun fromRequest(value: String, request: AuthRequest): AuthCode {
        val now = Instant.now()
        val notBefore = now.plusSeconds(tokenLifetimes.authorizationCode.notBeforeOffset)
        return AuthCode(
            value = value,
            clientId = request.clientId,
            userId = request.userId,
            scope = request.scope,
            issuedAt = now,
            notBefore = notBefore,
            expiresAt = notBefore.plusSeconds(tokenLifetimes.authorizationCode.lifetime)
        )
    }
}