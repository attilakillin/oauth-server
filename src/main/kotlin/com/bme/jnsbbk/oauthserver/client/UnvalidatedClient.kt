package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/** An unvalidated version of [Client]. This should only be used as an entity class
 *  for incoming JSON requests, and as the input of client validators.
 *  In any other situation, [Client] should be used instead. */
class UnvalidatedClient (
    @JsonProperty("client_id")     val id: String?,
    @JsonProperty("client_secret") var secret: String?,
    val redirectUris: Set<String>?,
    var tokenEndpointAuthMethod: String?,
    val grantTypes: Set<String>?,
    val responseTypes: Set<String>?,
    @JsonDeserialize(using = SpacedSetDeserializer::class)
    val scope: Set<String>?,

) {
    @JsonIgnore
    val extraData = mutableMapOf<String, String>()

    @JsonAnySetter
    fun putExtraData(key: String, value: String) = extraData.put(key, value)
}