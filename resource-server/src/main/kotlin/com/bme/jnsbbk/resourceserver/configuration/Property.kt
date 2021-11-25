package com.bme.jnsbbk.resourceserver.configuration

import javax.persistence.Entity
import javax.persistence.Id

/** Represents a key-value pair entity. */
@Entity
data class Property(
    /** Unique key value, one for each property. */
    @Id val id: Key,
    /** The value of the property. */
    val value: String
) {
    /** Enum used as property keys. */
    enum class Key {
        ID, SECRET
    }
}
