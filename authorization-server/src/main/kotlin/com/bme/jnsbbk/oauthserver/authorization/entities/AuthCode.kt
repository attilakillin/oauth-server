@file:Suppress("JpaAttributeTypeInspection")

package com.bme.jnsbbk.oauthserver.authorization.entities

import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id

/**
 * An entity class representing an OAuth authorization code.
 *
 * Contains a unique value, the IDs of the client and the user,
 * the authorized scope, as well as different validity timestamps.
 *
 * If the [expiresAt] field is null, the code never expires.
 */
@Entity
data class AuthCode(
    /** The value of the code, a randomly generated string. Unique. */
    @Id val value: String,
    /** The ID of the client associated with the code. */
    val clientId: String,
    /** The ID of the user associated with the code. */
    val userId: String,
    /** The set of scopes any token will be authorized for, if created from this code. */
    val scope: Set<String>,
    /** A state variable sent by the client. Used for ID token generation. */
    val nonce: String?,
    /** Timestamp of the moment the code was issued. */
    val issuedAt: Instant,
    /** Earliest moment the code is valid. */
    val notBefore: Instant,
    /** Last moment the code is valid. */
    val expiresAt: Instant?
)

/** Checks whether the authorization code has expired or not. */
fun AuthCode.isExpired() = expiresAt?.isBefore(Instant.now()) ?: false

/** Checks whether the authorization code is currently valid or not judging by the timestamps. */
fun AuthCode.isTimestampValid(): Boolean {
    val now = Instant.now()
    return issuedAt.isBefore(now) && notBefore.isBefore(now) && !isExpired()
}
