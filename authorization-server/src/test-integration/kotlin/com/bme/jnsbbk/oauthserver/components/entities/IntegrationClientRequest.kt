package com.bme.jnsbbk.oauthserver.components.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetSerializer
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class IntegrationClientRequest(
    val redirectUris: Set<String>,
    @JsonSerialize(using = SpacedSetSerializer::class) val scope: Set<String>,
    val grantTypes: Set<String>? = null,
    val responseTypes: Set<String>? = null,
    val tokenEndpointAuthMethod: String? = null,
    @JsonIgnore val extraData: Map<String, String> = mapOf()
) {
    @JsonAnyGetter fun getAllExtra() = extraData
}
