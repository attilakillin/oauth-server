package com.bme.jnsbbk.oauthserver.authorization

import java.time.Instant

/** An entity class representing an OAuth authorization code. */
class AuthCode (
    val value: String,
    val clientId: String,
    val scope: Set<String>,
    val issuedAt: Instant,
    val expiresAt: Instant
) { companion object }

/** Creates an authorization code with the given [value] from the given [request]. */
fun AuthCode.Companion.fromRequest(value: String, request: AuthRequest, lifeInSeconds: Long) : AuthCode {
    return AuthCode (
        value,
        request.clientId,
        request.scope,
        Instant.now(),
        Instant.now().plusSeconds(lifeInSeconds)
    )
}