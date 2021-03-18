package com.bme.jnsbbk.oauthserver.client

import com.fasterxml.jackson.annotation.*

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Client(
    @JsonProperty("client_id")
    var id: String?,
    @JsonProperty("client_secret")
    var secret: String?,
    var redirectUris: Set<String> = setOf(),
    var tokenEndpointAuthMethod: String = "",
    var grantTypes: Set<String> = setOf(),
    var responseTypes: Set<String> = setOf(),
    var scopes: Set<String> = setOf()
) {
    @JsonIgnore
    var extraInfo = mutableMapOf<String, String>()

    @JsonAnySetter
    fun putInfo(key: String, value: String) = extraInfo.put(key, value)

    @JsonAnyGetter
    fun getInfo() = extraInfo
}