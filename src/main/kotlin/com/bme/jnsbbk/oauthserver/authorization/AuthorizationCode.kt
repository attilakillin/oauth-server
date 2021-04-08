package com.bme.jnsbbk.oauthserver.authorization

import java.time.Instant

data class AuthorizationCode (
    val value: String,
    val clientId: String,
    val scope: Set<String>,
    val issuedAt: Instant,
    val expiresAt: Instant
) {
    constructor (value: String, properties: Map<String, String>, lifeInSeconds: Long) : this (
        value,
        properties["client_id"]!!,
        properties["scope"]!!.split(" ").toSet(),
        Instant.now(),
        Instant.now().plusSeconds(lifeInSeconds)
    )
}
