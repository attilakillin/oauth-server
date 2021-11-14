@file:Suppress("unused")
package com.bme.jnsbbk.oauthserver.user.entities

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

/**
 * Entity class containing personal information related to a user.
 *
 * Instances of this class are contained in the [User] entity, and as such, the connection is one-sided,
 * you can't reach the user from their user info object. Contains a name, email, and address field, as
 * well as an automatically generated ID field used for database serialization.
 */
@Entity
data class UserInfo(
    val name: String = "",
    val email: String = "",
    val address: String = ""
) {
    @Id @GeneratedValue
    private val id: Long = 0

    companion object
}

/** Creates a new [UserInfo] object from nullable field values, replacing nulls with empty strings. */
fun UserInfo.Companion.fromNullable(name: String?, email: String?, address: String?): UserInfo =
    UserInfo(name ?: "", email ?: "", address ?: "")
