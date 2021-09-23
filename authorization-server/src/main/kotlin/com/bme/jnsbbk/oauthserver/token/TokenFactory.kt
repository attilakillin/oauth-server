package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.jwt.TokenJwtHandler
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenResponse
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class TokenFactory(
    val jwtHandler: TokenJwtHandler,
    val appConfig: AppConfig
) {
    /** Creates an access token with the given [value] and from the given [code]. */
    fun accessFromCode(value: String, code: AuthCode) =
        fromTemplate(value, code.getData(), appConfig.tokens.accessToken, TokenType.ACCESS)

    /** Creates a refresh token with the given [value] and from the given [code]. */
    fun refreshFromCode(value: String, code: AuthCode) =
        fromTemplate(value, code.getData(), appConfig.tokens.refreshToken, TokenType.REFRESH)

    /** Creates an access token with the given [value] and from the given [refresh] token. */
    fun accessFromRefresh(value: String, refresh: Token): Token {
        require(refresh.type == TokenType.REFRESH)
        return fromTemplate(value, refresh.getData(), appConfig.tokens.accessToken, TokenType.ACCESS)
    }

    /** Creates a JWT token response from the given [access] and [refresh] tokens. */
    fun responseJwtFromTokens(access: Token, refresh: Token): TokenResponse {
        val expiresIn = if (access.expiresAt == null) {
            null
        } else {
            Duration.between(Instant.now(), access.expiresAt).seconds
        }

        return TokenResponse(
            accessToken = jwtHandler.createSignedAccess(access),
            refreshToken = refresh.value,
            tokenType = "Bearer",
            expiresIn = expiresIn,
            scope = access.scope
        )
    }

    /** Template function that can create a token from many sources. */
    private fun fromTemplate(
        value: String,
        data: CommonData,
        times: AppConfig.Tokens.Lifespan,
        type: TokenType
    ): Token {
        val now = Instant.now()
        val notBefore = now.plusSeconds(times.notBeforeOffset)
        val expiresAt = if (times.lifespan == 0L) null else notBefore.plusSeconds(times.lifespan)

        return Token(
            value = value,
            type = type,
            clientId = data.clientId,
            userId = data.userId,
            scope = data.scope,
            issuedAt = now,
            notBefore = notBefore,
            expiresAt = expiresAt
        )
    }

    /* Because Tokens and AuthCodes are not related in any way, extension functions
     * and a new class is used to get the data needed in the fromTemplate() function. */
    private data class CommonData(
        val clientId: String,
        val userId: String,
        val scope: Set<String>
    )

    private fun AuthCode.getData() = CommonData(clientId, userId, scope)
    private fun Token.getData() = CommonData(clientId, userId, scope)
}
