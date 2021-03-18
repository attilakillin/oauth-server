package com.bme.jnsbbk.oauthserver.model

import com.fasterxml.jackson.annotation.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Client(
    @JsonProperty("client_id")
    var id: String?,
    @JsonProperty("client_secret")
    var secret: String?,
    @JsonProperty("redirect_uris")
    var redirectURIs: Set<String>?,
    @JsonProperty("token_endpoint_auth_method")
    var tokenEndpointAuthMethod: String?,
    @JsonProperty("grant_types")
    var grantTypes: Set<String>?,
    @JsonProperty("response_types")
    var responseTypes: Set<String>?,
    @JsonProperty("scopes")
    var scopes: Set<String>?
) {
    @JsonIgnore
    var extraInfo = mutableMapOf<String, String>()

    @JsonAnySetter
    fun putInfo(key: String,value: String) = extraInfo.put(key, value)

    @JsonAnyGetter
    fun getInfo() = extraInfo
}