package com.bme.jnsbbk.oauthserver.dal

import com.bme.jnsbbk.oauthserver.dal.converters.StringListConverter
import javax.persistence.*

@Entity
data class Client (
    @Id var ID: String?,
    var secret: String?,

    @Convert(converter = StringListConverter::class)
    var redirectUris: List<String>?,
    var tokenEndpointAuthMethod: String?,
    @Convert(converter = StringListConverter::class)
    var grantTypes: List<String>?,
    @Convert(converter = StringListConverter::class)
    var responseTypes: List<String>?,
    @Convert(converter = StringListConverter::class)
    var scopes: List<String>?,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_metadata")
    var metadata: Map<String, String>?
)