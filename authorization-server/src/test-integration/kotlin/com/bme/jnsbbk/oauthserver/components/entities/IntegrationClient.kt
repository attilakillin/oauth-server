package com.bme.jnsbbk.oauthserver.components.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class IntegrationClient(
    @JsonProperty("client_id") val id: String,
    @JsonProperty("client_secret") val secret: String?,
    val redirectUris: Set<String>,
    val tokenEndpointAuthMethod: String,
    val grantTypes: Set<String>,
    val responseTypes: Set<String>,
    @JsonDeserialize(using = SpacedSetDeserializer::class) val scope: Set<String>,
    @JsonProperty("client_id_issued_at") val idIssuedAt: Instant,
    @JsonProperty("client_secret_expires_at") val secretExpiresAt: Instant?,
    val registrationAccessToken: String,
    @JsonIgnore val extraData: MutableMap<String, String> = mutableMapOf()
) {
    @JsonAnySetter fun putExtraData(key: String, value: String) = extraData.put(key, value)
}