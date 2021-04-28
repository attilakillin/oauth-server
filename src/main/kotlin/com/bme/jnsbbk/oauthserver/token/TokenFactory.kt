package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.config.TokenLifetimes
import com.bme.jnsbbk.oauthserver.jwt.TokenJwtHandler
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenResponse
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

/** Factory class to create tokens from specific templates. */
@Service
class TokenFactory (
    val tokenLifetimes: TokenLifetimes,
    val jwtHandler: TokenJwtHandler
) {
    /** Creates a token with the specified parameters. Used to reduce code duplication below. */
    private fun fromTemplate(value: String, code: AuthCode,
                             times: TokenLifetimes.LifetimeConfig, type: TokenType): Token {
        val now = Instant.now()
        val notBefore = now.plusSeconds(times.notBeforeOffset)
        return Token(
            value = value,
            type = type,
            clientId = code.clientId,
            userId = code.userId,
            scope = code.scope,
            issuedAt = now,
            notBefore = notBefore,
            expiresAt = notBefore.plusSeconds(times.lifetime)
        )
    }

    /** Creates an access token with the given [value] and from the given [code]. */
    fun accessFromCode(value: String, code: AuthCode) =
        fromTemplate(value, code, tokenLifetimes.accessToken, TokenType.ACCESS)

    /** Creates a refresh token with the given [value] and from the given [code]. */
    fun refreshFromCode(value: String, code: AuthCode) =
        fromTemplate(value, code, tokenLifetimes.refreshToken, TokenType.REFRESH)

    /** Creates an access token with the given [value] and from the given [refresh] token. */
    fun accessFromRefresh(value: String, refresh: Token): Token {
        require(refresh.type == TokenType.REFRESH)

        val now = Instant.now()
        val notBefore = now.plusSeconds(tokenLifetimes.accessToken.notBeforeOffset)
        return Token(
            value = value,
            type = TokenType.ACCESS,
            clientId = refresh.clientId,
            userId = refresh.userId,
            scope = refresh.scope,
            issuedAt = now,
            notBefore = notBefore,
            expiresAt = notBefore.plusSeconds(tokenLifetimes.accessToken.lifetime)
        )
    }

    /** Creates a JWT token response from the given [access] and [refresh] tokens. */
    fun responseJWTFromTokens(access: Token, refresh: Token): TokenResponse {
        return TokenResponse(
            accessToken = jwtHandler.createSigned(access),
            refreshToken = refresh.value, // TODO This should be a JWT token too (JTI field should be implemented)
            tokenType = "Bearer",
            expiresIn = Duration.between(Instant.now(), access.expiresAt).seconds,
            scope = access.scope
        )
    }
}