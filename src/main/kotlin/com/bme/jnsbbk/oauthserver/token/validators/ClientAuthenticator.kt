package com.bme.jnsbbk.oauthserver.token.validators

import com.bme.jnsbbk.oauthserver.client.entities.Client
import org.springframework.stereotype.Service

/**
 * Base interface for client authentication used in the token controller.
 *
 * As clients can be authenticated in multiple ways, this logic is separated into a bean.
 * Although it's named ClientAuthenticator, it's most relevant to the token controller,
 * as this type of client authentication occurs here.
 */
@Service
interface ClientAuthenticator {

    /**
     * Authenticate a client from the given parameters.
     *
     * The client can authenticate itself using an authorization header, or in URL encoded
     * form parameters, so both parts are passed to the function.
     *
     * Returns the client if the authentication was successful, or null, if it wasn't.
     */
    fun validClientOrNull(authHeader: String?, params: Map<String, String>): Client?
}