package com.bme.jnsbbk.resourceserver.resources

import javax.persistence.Entity
import javax.persistence.Id

/** Entity class that stores a notes string associated with a given user. */
@Entity
data class UserData(
    /** The username the notes string is associated with. */
    @Id val username: String,
    /** The notes string, used as an example for an OAuth protected resource. */
    val notes: String = ""
)
