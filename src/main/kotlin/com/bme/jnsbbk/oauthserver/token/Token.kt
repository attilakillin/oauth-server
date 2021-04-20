@file:Suppress("JpaAttributeTypeInspection")
package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.AuthCode
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Token (
    @Id
    val value: String,
    val type: TokenType,
    val clientId: String,
    val scope: Set<String>,
    val issuedAt: Instant,
    val expiresAt: Instant
) {
    companion object;

    constructor (value: String, type: TokenType, clientId: String,
                 scope: Set<String>, lifeInSeconds: Long) : this (
        value, type, clientId, scope, Instant.now(), Instant.now().plusSeconds(lifeInSeconds)
    )
}

enum class TokenType {
    ACCESS, REFRESH
}

fun Token.Companion.accessFromCode(value: String, code: AuthCode, lifeInSeconds: Long): Token {
    return Token(
        value = value,
        type = TokenType.ACCESS,
        clientId = code.clientId,
        scope = code.scope,
        lifeInSeconds = lifeInSeconds
    )
}

fun Token.Companion.refreshFromCode(value: String, code: AuthCode, lifeInSeconds: Long): Token {
    return Token(
        value = value,
        type = TokenType.REFRESH,
        clientId = code.clientId,
        scope = code.scope,
        lifeInSeconds = lifeInSeconds
    )
}

fun Token.Companion.accessFromRefresh(value: String, refreshToken: Token, lifeInSeconds: Long): Token {
    require(refreshToken.type == TokenType.REFRESH)
    return Token(
        value = value,
        type = TokenType.ACCESS,
        clientId = refreshToken.clientId,
        scope = refreshToken.scope,
        lifeInSeconds = lifeInSeconds
    )
}

fun Token.toUnsignedJWT() {

}