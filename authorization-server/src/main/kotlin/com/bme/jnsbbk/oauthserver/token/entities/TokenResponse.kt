package com.bme.jnsbbk.oauthserver.token.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetSerializer
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonSerialize

/**
 * An entity class representing a JSON response for token requests.
 *
 * The token requests of clients are answered with instances of this class.
 * If the [expiresIn] field is null, it's a hint to the client that the token never expires.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class TokenResponse(
    /** The main part of the response, contains an access token JWT or ID string. Mandatory. */
    val accessToken: String,
    /** May contain a refresh token. Optional. */
    val refreshToken: String?,
    /** The type of the tokens that are contained in the response. Usually 'Bearer'. */
    val tokenType: String,
    /** A hint to the client on how long the access token is valid for. */
    val expiresIn: Long?,
    /** The set of scopes the access token is authorized for. */
    @JsonSerialize(using = SpacedSetSerializer::class) val scope: Set<String>
) {
    /** An optional parameter, sent as part of the OpenID Connect specification. */
    var idToken: String? = null
}
