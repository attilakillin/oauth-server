package com.bme.jnsbbk.oauthserver.client.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * A nullable, request version of [Client].
 *
 * This class should only be used for incoming JSON requests, and as the input of
 * client validation. In any other situation, [Client] should be used instead.
 *
 * @see Client
 */
data class ClientRequest(
    /** The ID of the client. Created at validation, is unique. May be null in the request. */
    @JsonProperty("client_id") val id: String?,
    /** The secret of the client. May be null in the request. */
    @JsonProperty("client_secret") val secret: String?,
    /** The allowed redirection URIs of the client. Must not be null. */
    val redirectUris: Set<String>?,
    /** The auth method used at the token endpoint. May be null in the request. */
    val tokenEndpointAuthMethod: String?,
    /** All accepted grant types. May be null in the request. */
    val grantTypes: Set<String>?,
    /** Every accepted response type. May be null in the request. */
    val responseTypes: Set<String>?,
    /** The set of scopes the client is authorized to request. Must not be null. */
    @JsonDeserialize(using = SpacedSetDeserializer::class) val scope: Set<String>?
) {
    /** All extra data is stored in this string map. */
    @JsonIgnore
    val extraData = mutableMapOf<String, String>()

    /** Used by Jackson to parse every extra field that needs to be stored about the client. */
    @JsonAnySetter
    fun putExtraData(key: String, value: String) = extraData.put(key, value)
}
