package com.bme.jnsbbk.oauthserver.authorization.entities

/** An entity class representing an OAuth authorization request.
 *  This is the validated equivalent of [UnvalidatedAuthRequest]. */
class AuthRequest (
    val clientId: String,
    val redirectUri: String,
    val responseType: String,
    var scope: Set<String>,
    val state: String?
) {
    /* Lateinit, because the user is not yet authenticated when the request is created. */
    lateinit var userId: String
}