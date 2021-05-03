package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.config.AppConfig
import org.springframework.stereotype.Service
import java.time.Instant

/** Factory class to create authorization codes from specific templates. */
@Service
class AuthCodeFactory(val appConfig: AppConfig) {
    /**
     * Creates an authorization code with the given [value] and from the given [request].
     *
     * Timestamps are generated based on the application configuration.
     *
     * @see AppConfig
     */
    fun fromRequest(value: String, request: AuthRequest): AuthCode {
        val now = Instant.now()
        val notBefore = now.plusSeconds(appConfig.tokens.authCode.notBeforeOffset)
        val lifespan = appConfig.tokens.authCode.lifespan
        val expiresAt = if (lifespan == 0L) null else now.plusSeconds(lifespan)

        return AuthCode(
            value = value,
            clientId = request.clientId,
            userId = request.userId,
            scope = request.scope,
            issuedAt = now,
            notBefore = notBefore,
            expiresAt = expiresAt
        )
    }
}
