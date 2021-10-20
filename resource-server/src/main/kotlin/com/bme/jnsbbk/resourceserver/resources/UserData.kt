package com.bme.jnsbbk.resourceserver.resources

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class UserData(
    @Id val username: String,
    val email: String?,
    val address: String?
)
