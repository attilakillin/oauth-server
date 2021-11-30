package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.token.TokenService
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val authCodeFactory: AuthCodeFactory,
    private val authCodeRepository: AuthCodeRepository,
    private val clientRepository: ClientRepository,
    private val tokenService: TokenService
) {

    /** Returns true if the client contained in the authorization request is valid. */
    fun validateRequestClient(request: UnvalidatedAuthRequest): Boolean {
        return request.clientId != null && clientRepository.existsById(request.clientId)
    }

    /** Extracts the client referred to by the request clientId attribute. */
    fun getClientFrom(request: UnvalidatedAuthRequest): Client {
        requireNotNull(request.clientId)
        val client = clientRepository.findByIdOrNull(request.clientId)

        return requireNotNull(client)
    }

    /**
     * Returns true if the request contains a valid redirect URI, or if it contains no URI,
     * but the client only has one URI registered.
     */
    fun validateRequestUri(request: UnvalidatedAuthRequest): Boolean {
        val client = getClientFrom(request)

        return if (client.redirectUris.size == 1) {
            request.redirectUri in client.redirectUris || request.redirectUri == null
        } else {
            request.redirectUri in client.redirectUris
        }
    }

    /** Returns true if the response type in the request is a valid response type of the client. */
    fun validateRequestResponseType(request: UnvalidatedAuthRequest): Boolean {
        val client = getClientFrom(request)
        return request.responseType in client.responseTypes
    }

    /** Returns true if the scope in the request is a valid subset of the client's scope. */
    fun validateRequestScope(request: UnvalidatedAuthRequest): Boolean {
        val client = getClientFrom(request)
        return request.scope == null || request.scope.all { it in client.scope }
    }

    /** Converts the authorization request into a null-safe, parsed request object. */
    fun convertToValidRequest(request: UnvalidatedAuthRequest): AuthRequest {
        val client = getClientFrom(request)

        return AuthRequest(
            clientId = client.id,
            redirectUri = request.redirectUri ?: client.redirectUris.first(),
            responseType = requireNotNull(request.responseType),
            scope = request.scope ?: client.scope,
            state = request.state,
            nonce = request.nonce
        )
    }

    /** Extracts prefixed scope keys from the given map and returns them as a string set. */
    fun extractPrefixedScopes(map: Map<String, String>, prefix: String): Set<String> {
        return map.keys
            .filter { it.startsWith(prefix) }
            .map { it.removePrefix(prefix) }
            .distinct()
            .toSet()
    }

    /** Creates and saves an authorization code object from the given request. */
    fun createAuthCode(request: AuthRequest): AuthCode {
        val value = RandomString.generateUntil(16) { !authCodeRepository.existsById(it) }
        return authCodeRepository.save(authCodeFactory.fromRequest(value, request))
    }

    /** Creates an implicit flow token response from an auth request. */
    fun createImplicitResponse(request: AuthRequest): Map<String, String?> {
        val response = tokenService.createResponseWithJustAccessToken(request.clientId, request.userId, request.scope)

        return mapOf(
            "access_token" to response.accessToken,
            "token_type" to response.tokenType,
            "scope" to response.scope.joinToString(" "),
            "state" to request.state
        )
    }
}
