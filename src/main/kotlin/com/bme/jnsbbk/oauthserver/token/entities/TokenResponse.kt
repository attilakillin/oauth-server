package com.bme.jnsbbk.oauthserver.token.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetSerializer
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize

/**
 * An entity class representing a JSON response for token requests.
 *
 * The token requests of clients are answered with instances of this class.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TokenResponse (
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String,
    val expiresIn: Long,

    @JsonSerialize(using = SpacedSetSerializer::class)
    val scope: Set<String>
)