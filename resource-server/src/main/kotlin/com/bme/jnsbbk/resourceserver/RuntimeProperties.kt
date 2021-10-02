package com.bme.jnsbbk.resourceserver

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Property(
    @Id val id: Key,
    val value: String
)

enum class Key {
    ID, SECRET, SCOPE
}

@Repository
interface PropertyRepository : JpaRepository<Property, Key>
