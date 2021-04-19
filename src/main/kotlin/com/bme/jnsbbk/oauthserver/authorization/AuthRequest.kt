package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class AuthRequest (
    val clientId: String?,
    var redirectUri: String?,
    val responseType: String?,
    val state: String?,

    @JsonDeserialize(using = SpacedSetDeserializer::class)
    var scope: MutableSet<String> = mutableSetOf()
)