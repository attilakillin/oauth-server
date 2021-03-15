package com.bme.jnsbbk.oauthserver.dal

import com.bme.jnsbbk.oauthserver.dal.converters.StringListConverter
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Client (
    @Id var ID: String,
    var secret: String,

    @Convert(converter = StringListConverter::class)
    var redirectURIs: List<String>,
    var tokenEndpointAuthMethod: String,
    @Convert(converter = StringListConverter::class)
    var grantTypes: List<String>,
    @Convert(converter = StringListConverter::class)
    var responseTypes: List<String>,
    @Convert(converter = StringListConverter::class)
    var scopes: List<String>,
    var metadata: String
)