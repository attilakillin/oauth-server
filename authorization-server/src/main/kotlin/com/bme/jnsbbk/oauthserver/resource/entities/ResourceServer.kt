@file:Suppress("JpaAttributeTypeInspection")
package com.bme.jnsbbk.oauthserver.resource.entities

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "ResourceServer")
data class ResourceServer(
    @Id val id: String,
    @Column(unique = true)
    val url: String,
    val scope: Set<String>
)
