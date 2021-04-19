package com.bme.jnsbbk.oauthserver.authorization

import java.time.Instant

data class AuthCode (
    val value: String,
    val clientId: String,
    val scope: Set<String>,
    val issuedAt: Instant,
    val expiresAt: Instant
) { companion object }

fun AuthCode.Companion.fromRequest(value: String, request: AuthRequest, lifeInSeconds: Long) : AuthCode {
    return AuthCode (
        value,
        request.clientId!!,
        request.scope,
        Instant.now(),
        Instant.now().plusSeconds(lifeInSeconds)
    )
}