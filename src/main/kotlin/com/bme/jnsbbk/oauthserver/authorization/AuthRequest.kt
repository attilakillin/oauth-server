package com.bme.jnsbbk.oauthserver.authorization

/** An entity class representing an OAuth authorization request.
 *  This is the validated equivalent of [UnvalidatedAuthRequest]. */
class AuthRequest (
    val clientId: String,
    val redirectUri: String,
    val responseType: String,
    var scope: Set<String>,
    val state: String?
)