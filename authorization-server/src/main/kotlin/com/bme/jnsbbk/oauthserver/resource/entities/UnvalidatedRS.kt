package com.bme.jnsbbk.oauthserver.resource.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class UnvalidatedRS (
    @JsonDeserialize(using = SpacedSetDeserializer::class)
    val scope: Set<String>
)
