package com.bme.jnsbbk.oauthserver.token.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetSerializer
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TokenResponse (
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String,
    val expiresIn: Long,

    @JsonSerialize(using = SpacedSetSerializer::class)
    val scope: Set<String>
)