package com.bme.jnsbbk.oauthserver.users

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class User (
    @Id val id: String,
    @Column(unique = true)
    val email: String,
    val passwordHash: String
)