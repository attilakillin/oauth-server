@file:Suppress("JpaAttributeTypeInspection")
package com.bme.jnsbbk.oauthserver.token.entities

import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Token (
    @Id val value: String,
    val type: TokenType,
    val clientId: String,
    val userId: String,
    val scope: Set<String>,
    val issuedAt: Instant,
    val notBefore: Instant,
    val expiresAt: Instant
)

enum class TokenType {
    ACCESS, REFRESH
}