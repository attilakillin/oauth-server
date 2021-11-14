package com.bme.jnsbbk.oauthserver.resource.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * A data class representing an incoming registration request from an OAuth resource server.
 *
 * Has the exact same attributes as a [ResourceServer] object, but every attribute is nullable.
 * Can be implicitly created by a Jackson deserialization process.
 *
 * Used only during registration. If a resource server instance is created, it is done using the
 * [ResourceServer] server class, see that for further details.
 */
data class ResourceServerRequest(
    val id: String?,
    val secret: String?,
    val baseUrl: String?,
    @JsonDeserialize(using = SpacedSetDeserializer::class)
    val scope: Set<String>
)
