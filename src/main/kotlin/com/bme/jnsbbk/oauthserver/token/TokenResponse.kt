package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.utils.SpacedSetSerializer
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.time.Duration
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TokenResponse (
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String,
    val expiresIn: Long,

    @JsonSerialize(using = SpacedSetSerializer::class)
    val scope: Set<String>
) { companion object }

fun TokenResponse.Companion.fromTokens(access: Token, refresh: Token): TokenResponse {
    return TokenResponse (
        access.toUnsignedJWT(),
        refresh.value,
        "Bearer",
        Duration.between(Instant.now(), access.expiresAt).seconds,
        access.scope
    )
}