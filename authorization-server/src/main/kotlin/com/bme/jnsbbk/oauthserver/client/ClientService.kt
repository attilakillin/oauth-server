package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.entities.ClientRequest
import com.bme.jnsbbk.oauthserver.utils.*
import com.bme.jnsbbk.oauthserver.wellknown.ServerMetadata
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ClientService(
    private val clientRepository: ClientRepository
) {
    /** Authenticate a client based on its ID and secret. Returns null if unauthorized. */
    fun authenticate(id: String, secret: String): Client? {
        val client = clientRepository.findByIdOrNull(id) ?: return null
        return if (client.secret == secret) client else null
    }

    /** Authenticate a client using HTTP Basic authentication. Returns null if unauthorized. */
    fun authenticateBasic(authHeader: String): Client? {
        val (id, secret) = authHeader.decodeAsHttpBasic() ?: return null
        return authenticate(id, secret)
    }

    /** Authenticate a client using parameters given with a String Map. Returns null if unauthorized. */
    fun authenticateParam(parameters: Map<String, String>): Client? {
        val id = parameters["client_id"]
        val secret = parameters["client_secret"]
        if (id == null || secret == null) return null
        return authenticate(id, secret)
    }

    /**
     * Authenticate a client with either HTTP Basic or request parameter authentication.
     *
     * Returns null if unauthorized, or if both methods are used simultaneously.
     */
    fun authenticateWithEither(authHeader: String?, parameters: Map<String, String>): Client? {
        val client1 = authenticateBasic(authHeader ?: "")
        val client2 = authenticateParam(parameters)

        return if (client1 != null && client2 != null) {
            null
        } else {
            client1 ?: client2
        }
    }

    /**
     * Validates most fields of a client [request].
     *
     * Returns true if the request passes validation. This means that every required field must be present,
     * the grant types, response types and the auth method must be one accepted by the server (or null, in
     * which case the server will fill these fields), and the request must not contain invalid characters in
     * its string sets and invalid fields in the request object.
     */
    private fun validateClientRequestFields(request: ClientRequest): Boolean {
        val requiredPresent = request.redirectUris.isNotNullOrEmpty() && request.scope.isNotNullOrEmpty()

        val grantsValid = request.grantTypes.isNullOrAll { it in ServerMetadata.grantTypesSupported }
        val responsesValid = request.responseTypes.isNullOrAll { it in ServerMetadata.responseTypesSupported }
        val authMethodValid =
            request.tokenEndpointAuthMethod.isNullOr { it in ServerMetadata.tokenEndpointAuthMethodsSupported }

        val invalidFields = listOf("client_id_issued_at", "client_id_expires_at", "registration_client_uri")
        val extrasValid = request.extraData.none { it.key in invalidFields }

        val sets = listOfNotNull(request.redirectUris, request.grantTypes, request.responseTypes, request.scope)
        val charsValid = sets.all { set -> set.none { string -> StringSetConverter.SEPARATOR in string } }

        return requiredPresent && grantsValid && responsesValid && authMethodValid && extrasValid && charsValid
    }

    /** Validates a new client registration request. Returns true if the request passes validation. */
    fun validateClientRegistration(request: ClientRequest): Boolean {
        return validateClientRequestFields(request) && request.id == null && request.secret == null
    }

    /** Validates a client update request. Returns true if the request passes validation. */
    fun validateClientUpdate(client: Client, request: ClientRequest): Boolean {
        return validateClientRequestFields(request) && request.id == client.id && request.secret == client.secret
    }

    /** A map of each accepted grant type to its respective response type. */
    private val grantPairs = mapOf("authorization_code" to "code", "implicit" to "token")

    /** Synchronizes the grant types and response types present (or not present) in the request. */
    private fun syncGrantsAndResponses(request: ClientRequest): Pair<Set<String>, Set<String>> {
        val grants = request.grantTypes?.toMutableSet() ?: mutableSetOf("authorization_code")
        val responses = request.responseTypes?.toMutableSet() ?: mutableSetOf()
        grants.addAll(grantPairs.filter { (_, r) -> r in responses }.keys)
        responses.addAll(grantPairs.filter { (g, _) -> g in grants }.values)

        return Pair(grants, responses)
    }

    /** Creates a valid client object from the given [request]. */
    fun createValidClient(request: ClientRequest): Client {
        val secret = if (request.tokenEndpointAuthMethod != "none") RandomString.generate(48) else null
        val (grants, responses) = syncGrantsAndResponses(request)

        val client = Client(
            id = RandomString.generateUntil { !clientRepository.existsById(it) },
            secret = secret,
            redirectUris = requireNotNull(request.redirectUris),
            tokenEndpointAuthMethod = request.tokenEndpointAuthMethod ?: "client_secret_basic",
            grantTypes = grants,
            responseTypes = responses,
            scope = requireNotNull(request.scope),
            idIssuedAt = Instant.now(),
            secretExpiresAt = null,
            registrationAccessToken = RandomString.generate()
        )
        client.extraData.putAll(request.extraData)

        return client
    }

    /** Creates a valid client object from the given [request], as an updated version of the [original] client. */
    fun updateValidClient(original: Client, request: ClientRequest): Client {
        val (grants, responses) = syncGrantsAndResponses(request)

        val client = Client(
            id = original.id,
            secret = original.secret,
            redirectUris = requireNotNull(request.redirectUris),
            tokenEndpointAuthMethod = request.tokenEndpointAuthMethod ?: "client_secret_basic",
            grantTypes = grants,
            responseTypes = responses,
            scope = requireNotNull(request.scope),
            idIssuedAt = original.idIssuedAt,
            secretExpiresAt = original.secretExpiresAt,
            registrationAccessToken = original.registrationAccessToken
        )
        client.extraData.putAll(request.extraData)

        return client
    }

    /**
     * Given a client [id], and an authorization header, find a client that matches the [authHeader], and return it.
     *
     * Returns the client, or null, if no such client exists.
     */
    fun getAuthorizedClient(id: String, authHeader: String?): Client? {
        val client = clientRepository.findByIdOrNull(id)
        if (client == null || authHeader == null) return null

        return if (client.registrationAccessToken == authHeader.removePrefix("Bearer ")) client else null
    }
}
