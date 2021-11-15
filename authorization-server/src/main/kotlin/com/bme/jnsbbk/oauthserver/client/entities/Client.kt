@file:Suppress("JpaAttributeTypeInspection")
package com.bme.jnsbbk.oauthserver.client.entities

import com.bme.jnsbbk.oauthserver.utils.SpacedSetSerializer
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.time.Instant
import javax.persistence.*

/**
 * An entity class representing an OAuth client.
 *
 * This class represents a valid instance of an OAuth client, with all its properties.
 * Most properties are lateinit, as the client instance is created gradually during
 * validation.
 *
 * Less important properties are stored in the [extraData] string map.
 *
 * @see ClientRequest
 */
@Entity
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class Client(
    /** The ID of the client. Created at validation, is unique. */
    @JsonProperty("client_id") @Id val id: String,
    /** The secret of the client. Depending on the auth method, may be null. */
    @JsonProperty("client_secret") val secret: String?,
    /** The allowed redirection URIs of the client. */
    val redirectUris: Set<String>,
    /** The auth method used at the token endpoint. May be "none" if the client doesn't use the token endpoint. */
    val tokenEndpointAuthMethod: String,
    /** All accepted grant types. */
    val grantTypes: Set<String>,
    /** Every accepted response type. */
    val responseTypes: Set<String>,
    /** The set of scopes the client is authorized to request. */
    @JsonSerialize(using = SpacedSetSerializer::class) val scope: Set<String>,
    /** The instant the client ID was issued at. */
    @JsonProperty("client_id_issued_at") val idIssuedAt: Instant,
    /** The last moment the client's secret is valid before expiration. May be null if it never expires. */
    @JsonProperty("client_secret_expires_at") val secretExpiresAt: Instant? = null,
    /** A random string that the client uses to manage its registration. */
    val registrationAccessToken: String
) {
    /** All extra data is stored in this string map. */
    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_extra_data")
    val extraData = mutableMapOf<String, String>()

    /** Used by Jackson to retrieve every extra field stored about the client. */
    @JsonAnyGetter
    fun getAllExtra() = extraData
}
