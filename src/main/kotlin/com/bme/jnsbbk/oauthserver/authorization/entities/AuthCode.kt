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
 */
@Entity
data class AuthCode (
    @Id val value: String,
    val clientId: String,
    val userId: String,
    val scope: Set<String>,
    val issuedAt: Instant,
    val notBefore: Instant,
    val expiresAt: Instant
)

/** Checks whether the authorization code is currently valid or not judging by the timestamps. */
fun AuthCode.isTimestampValid(): Boolean {
    val now = Instant.now()
    return issuedAt.isBefore(now) && notBefore.isBefore(now) && expiresAt.isAfter(now)
}