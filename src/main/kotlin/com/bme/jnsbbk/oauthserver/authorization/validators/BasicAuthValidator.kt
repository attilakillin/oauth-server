package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/** Reference implementation of [AuthValidator]. Validates the requested [UnvalidatedAuthRequest],
 *  and converts it into a valid [AuthRequest]. */
@Service
class BasicAuthValidator : AuthValidator {
    @Autowired private lateinit var clientRepository: ClientRepository

    /** Validates sensitive information. The expected return value of this method is null.
     *  If the method returns something else, the resource owner must not be redirected to
     *  the client redirect URI!
     *
     *  Validated fields are updated in the [request] object. */
    override fun validateSensitiveOrError(request: UnvalidatedAuthRequest): String? {
        val client = request.clientId?.let { clientRepository.findById(it).getOrNull() }
            ?: return "Unknown client"

        val uri = request.redirectUri
        if (uri == null) {
            if (client.redirectUris.size != 1) return "Invalid redirect URI!"
            request.redirectUri = client.redirectUris.first()
        } else {
            if (uri !in client.redirectUris) return "Invalid redirect URI!"
        }
        return null
    }

    /** Validates additional information. The method requires that the [validateSensitiveOrError]
     *  method be called before this, otherwise it can throw exceptions.
     *
     *  The expected return value of this method is null. If a string is returned, the resource
     *  owner can safely be redirected to the client redirect URI (with an error).
     *
     *  Validated fields are updated in the [request] object. */
    override fun validateAdditionalOrError(request: UnvalidatedAuthRequest): String? {
        requireNotNull(request.clientId)

        val client = clientRepository.findById(request.clientId).get()

        if (request.responseType !in client.responseTypes)
            return "unsupported_response_type"

        request.scope?.forEach { if (it !in client.scope) return "invalid_scope" }
        if (request.scope == null) request.scope = client.scope
        return null
    }

    /** Converts the [request] object into a validated [AuthRequest].
     *  Throws an exception if any of the (otherwise required) fields are null.
     *  To avoid these exceptions, always call the two validation methods before calling this. */
    override fun convertToValidRequest(request: UnvalidatedAuthRequest): AuthRequest {
        return AuthRequest(
            clientId = request.clientId!!,
            redirectUri = request.redirectUri!!,
            responseType = request.responseType!!,
            scope = request.scope!!,
            state = request.state
        )
    }
}