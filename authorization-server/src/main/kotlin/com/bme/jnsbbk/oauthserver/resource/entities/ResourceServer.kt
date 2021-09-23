@file:Suppress("JpaAttributeTypeInspection")
package com.bme.jnsbbk.oauthserver.resource.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetSerializer
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "ResourceServer")
data class ResourceServer(
    @Id val id: String,
    val secret: String,
    @Column(unique = true)
    val url: String,
    @JsonSerialize(using = SpacedSetSerializer::class)
    val scope: Set<String>
)
