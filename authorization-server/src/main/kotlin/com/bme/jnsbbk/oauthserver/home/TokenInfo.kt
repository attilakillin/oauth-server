package com.bme.jnsbbk.oauthserver.home

import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.isTimestampValid

/**
 * A data class containing displayable information about a token.
 *
 * Used in the [HomeController] to display rows of information about authorized tokens.
 */
data class TokenInfo(
    val value: String,
    val type: String,
    val clientId: String,
    val scope: String,
    val active: String
) {
    companion object
}

/** Creates a [TokenInfo] view from a given [token]. */
fun TokenInfo.Companion.fromToken(token: Token): TokenInfo {
    return TokenInfo(
        value = token.value,
        type = token.type.name,
        clientId = token.clientId,
        scope = token.scope.joinToString(" "),
        active = if (token.isTimestampValid()) "Yes" else "No"
    )
}
