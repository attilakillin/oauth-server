package com.bme.jnsbbk.oauthserver.users.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

/**
 * Entity class representing an end user.
 *
 * These users are used as the resource owners of OAuth resources.
 *
 * Passwords must only be stored as hashed, preferably salted, encoded strings.
 */
@Entity
data class User (
    @Id val id: String,
    @Column(unique = true)
    val email: String,
    val passwordHash: String
)