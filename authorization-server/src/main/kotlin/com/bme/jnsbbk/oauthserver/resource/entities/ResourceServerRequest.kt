package com.bme.jnsbbk.oauthserver.resource.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/** A data class that represents an incoming registration request from an OAuth resource server. */
data class ResourceServerRequest(
    val id: String?,
    val secret: String?,
    val baseUrl: String?,
    @JsonDeserialize(using = SpacedSetDeserializer::class)
    val scope: Set<String>
)
