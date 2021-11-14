package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.config.AppConfig
import org.springframework.stereotype.Service
import java.time.Instant

/** Factory class to create authorization codes from specific templates. */
@Service
class AuthCodeFactory(appConfig: AppConfig) {
    private val config = appConfig.tokens.authCode

    /**
     * Creates an authorization code with the given [value] and from the given [request].
     *
     * Timestamps are generated based on the application configuration.
     *
     * @see AppConfig
     */
    fun fromRequest(value: String, request: AuthRequest): AuthCode {
        val now = Instant.now()
        val notBefore = now.plusSeconds(config.notBeforeOffset)
        val expiresAt = if (config.lifespan != 0L) notBefore.plusSeconds(config.lifespan) else null

        return AuthCode(
            value = value,
            clientId = request.clientId,
            userId = request.userId,
            scope = request.scope,
            nonce = request.nonce,
            issuedAt = now,
            notBefore = notBefore,
            expiresAt = expiresAt
        )
    }
}
