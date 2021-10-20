package com.bme.jnsbbk.resourceserver.configuration

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Property(
    @Id val id: Key,
    val value: String
) {
    enum class Key {
        ID, SECRET, SCOPE
    }
}
