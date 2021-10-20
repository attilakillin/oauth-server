package com.bme.jnsbbk.oauthserver.authorization.entities

/**
 * An entity class representing an OAuth authorization request.
 *
 * This data class represents a valid authorization request from an OAuth client.
 * After the user authorizes the request, an [AuthCode] can be constructed from the data here.
 *
 * @see UnvalidatedAuthRequest
 */
data class AuthRequest(
    val clientId: String,
    val redirectUri: String,
    val responseType: String,
    var scope: Set<String>,
    val state: String?,
    val nonce: String?
) {
    lateinit var userId: String // The user is not yet authenticated at creation
}
