package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.authorization.AuthRequest
import com.bme.jnsbbk.oauthserver.authorization.UnvalidatedAuthRequest
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.stereotype.Service

/** Reference implementation of [AuthValidator]. Validates the requested [UnvalidatedAuthRequest],
 *  and calls the relevant lambda based on whether the validation succeeded or failed. */
@Service
class BasicAuthValidator (
    val clientRepository: ClientRepository
) : AuthValidator {

    /** A complex validator method. Validates the given [request] and calls [success] if the
     *  validation succeeded. If the validation failed, and it was an authentication failure
     *  that should not result in a redirect, [errorIfNoRedirect] is called. If it was an otherwise
     *  minor error that can be safely communicated back to the client, [errorIfRedirect] is called.
     *
     *  The return value is the same as the return value of the lambda that was called during validation. */
    override fun validate(request: UnvalidatedAuthRequest,
                          errorIfNoRedirect: (String) -> String,
                          errorIfRedirect: (String, String) -> String,
                          success: (AuthRequest) -> String): String {
        val client = request.clientId?.let { clientRepository.findById(it).getOrNull() }
            ?: return errorIfNoRedirect("Unknown client")

        var uri = request.redirectUri
        if (uri == null) {
            if (client.redirectUris.size != 1) return errorIfNoRedirect("Invalid redirect URI!")
            uri = client.redirectUris.first()
        } else {
            if (uri !in client.redirectUris) return errorIfNoRedirect("Invalid redirect URI!")
        }

        if (request.responseType !in client.responseTypes)
            return errorIfRedirect(uri, "unsupported_response_type")

        val scope = request.scope ?: client.scope
        scope.forEach { if (it !in client.scope) return errorIfRedirect(uri, "invalid_scope") }

        return success(AuthRequest(client.id, uri, request.responseType!!, scope, request.state))
    }
}