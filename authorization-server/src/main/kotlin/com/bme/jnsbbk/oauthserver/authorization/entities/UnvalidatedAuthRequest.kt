package com.bme.jnsbbk.oauthserver.authorization.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetDeserializer
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * An unvalidated version of [AuthRequest].
 *
 * This class should only be used for incoming JSON requests, and as the input of
 * auth request validators. In any other situation, [AuthRequest] should be used instead.
 *
 * @see AuthRequest
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UnvalidatedAuthRequest(
    val clientId: String?,
    val redirectUri: String?,
    val responseType: String?,
    val state: String?,
    val nonce: String?,
    @JsonDeserialize(using = SpacedSetDeserializer::class)
    val scope: Set<String>?
)
