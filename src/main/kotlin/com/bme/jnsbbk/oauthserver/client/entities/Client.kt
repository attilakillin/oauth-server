@file:Suppress("JpaAttributeTypeInspection")
package com.bme.jnsbbk.oauthserver.client.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.bme.jnsbbk.oauthserver.utils.SpacedSetSerializer
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.time.Instant
import javax.persistence.*

/** An entity class representing an OAuth client. This is the validated equivalent of [UnvalidatedClient]. */
@Entity
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Client (
    @JsonProperty("client_id") @Id val id: String,
) {
    @JsonProperty("client_secret")
    var secret: String? = null

    lateinit var redirectUris: Set<String>
    lateinit var tokenEndpointAuthMethod: String
    lateinit var grantTypes: Set<String>
    lateinit var responseTypes: Set<String>

    @JsonSerialize(using = SpacedSetSerializer::class)
    @JsonDeserialize(using = SpacedSetDeserializer::class)
    lateinit var scope: Set<String>

    @JsonProperty("client_id_issued_at")
    lateinit var idIssuedAt: Instant

    @JsonProperty("client_secret_expires_at")
    var expiresAt: Instant? = null

    lateinit var registrationAccessToken: String

    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_extra_data")
    val extraData = mutableMapOf<String, String>()

    @JsonAnySetter
    fun putExtraData(key: String, value: String) = extraData.put(key, value)

    @JsonAnyGetter
    fun getAllExtra() = extraData
}