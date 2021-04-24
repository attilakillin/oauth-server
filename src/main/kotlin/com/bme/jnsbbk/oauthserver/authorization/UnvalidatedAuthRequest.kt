package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonNaming

/** An unvalidated version of [AuthRequest]. This should only be used as an entity
 *  class for incoming JSON requests, and as the input of auth request validators.
 *  In any other situation, [AuthRequest] should be used instead. */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
class UnvalidatedAuthRequest (
    val clientId: String?,
    val redirectUri: String?,
    val responseType: String?,
    val state: String?,
    @JsonDeserialize(using = SpacedSetDeserializer::class)
    val scope: Set<String>?
)