@file:Suppress("JpaAttributeTypeInspection")
package com.bme.jnsbbk.oauthserver.resource.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetSerializer
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

/**
 * An entity class representing an OAuth resource server.
 *
 * Each resource server must have an ID and a secret, its own URL, and a set of associated scopes.
 */
@Entity
data class ResourceServer(
    @Id val id: String,
    val secret: String,
    @Column(unique = true)
    val url: String,
    @JsonSerialize(using = SpacedSetSerializer::class)
    val scope: Set<String>
)
