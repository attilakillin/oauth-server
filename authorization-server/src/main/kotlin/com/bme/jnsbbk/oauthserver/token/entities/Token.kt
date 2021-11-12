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
 *
 * If the [expiresAt] field is null, the token never expires.
 */
@Entity
data class Token(
    /** The value of the token, a randomly generated string. Unique. */
    @Id val value: String,
    /** The type of the token, either access or refresh. */
    val type: TokenType,
    /** The ID of the client associated with the token. */
    val clientId: String,
    /** The ID of the user associated with the token. */
    val userId: String?,
    /** The set of scopes the token is authorized for. */
    val scope: Set<String>,
    /** Timestamp of the moment the token was issued. */
    val issuedAt: Instant,
    /** Earliest moment the token is valid. */
    val notBefore: Instant,
    /** Last moment the token is valid. */
    val expiresAt: Instant?
)

/** An enum class representing the different OAuth token types. */
enum class TokenType {
    ACCESS, REFRESH
}

/** Checks whether the token has expired or not. */
fun Token.isExpired() = expiresAt?.isBefore(Instant.now()) ?: false

/** Checks whether the token is currently valid or not judging by the timestamps. */
fun Token.isTimestampValid(): Boolean {
    val now = Instant.now()
    return issuedAt.isBefore(now) && notBefore.isBefore(now) && !isExpired()
}
