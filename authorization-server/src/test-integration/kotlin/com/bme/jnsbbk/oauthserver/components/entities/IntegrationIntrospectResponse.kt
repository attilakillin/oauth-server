package com.bme.jnsbbk.oauthserver.components.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class IntegrationIntrospectResponse (
    val active: String,
    val iss: String?,
    val sub: String?,
    @JsonDeserialize(using = SpacedSetDeserializer::class) val scope: Set<String>?,
    val clientId: String?,
    val username: String?
)
