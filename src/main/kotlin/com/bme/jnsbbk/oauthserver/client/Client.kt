package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.bme.jnsbbk.oauthserver.utils.SpacedSetSerializer
import com.bme.jnsbbk.oauthserver.utils.StringSetConverter
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.time.Instant
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Client(
    @JsonProperty("client_id") @Id
    var id: String?,

    @JsonProperty("client_secret")
    var secret: String?,

    @Convert(converter = StringSetConverter::class)
    var redirectUris: Set<String> = setOf(),

    var tokenEndpointAuthMethod: String = "",

    @Convert(converter = StringSetConverter::class)
    var grantTypes: MutableSet<String> = mutableSetOf(),

    @Convert(converter = StringSetConverter::class)
    var responseTypes: MutableSet<String> = mutableSetOf(),

    @Convert(converter = StringSetConverter::class)
    @JsonSerialize(using = SpacedSetSerializer::class)
    @JsonDeserialize(using = SpacedSetDeserializer::class)
    var scope: Set<String> = setOf(),

    @JsonProperty("client_id_issued_at")
    var idIssuedAt: Instant?,

    @JsonProperty("client_secret_expires_at")
    var expiresAt: Instant?,

    var registrationAccessToken: String?
) {
    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_metadata")
    var extraInfo = mutableMapOf<String, String>()

    @JsonAnySetter
    fun putInfo(key: String, value: String) = extraInfo.put(key, value)

    @JsonAnyGetter
    fun getInfo() = extraInfo
}