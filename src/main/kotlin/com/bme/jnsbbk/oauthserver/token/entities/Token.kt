@file:Suppress("JpaAttributeTypeInspection")
package com.bme.jnsbbk.oauthserver.token.entities

import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id

/**
 * An entity class representing an OAuth access or refresh token.
 *
 * As the two token types are very similar, they are stored in a common
 * class, only differentiated by the [type] property.
 */
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

/** An enum class representing the different OAuth token types. */
enum class TokenType {
    ACCESS, REFRESH
}

/** Checks whether the token has expired or not. */
fun Token.isExpired(): Boolean = expiresAt.isBefore(Instant.now())

/** Checks whether the token is currently valid or not judging by the timestamps. */
fun Token.isTimestampValid(): Boolean {
    val now = Instant.now()
    return issuedAt.isBefore(now) && notBefore.isBefore(now) && expiresAt.isAfter(now)
}