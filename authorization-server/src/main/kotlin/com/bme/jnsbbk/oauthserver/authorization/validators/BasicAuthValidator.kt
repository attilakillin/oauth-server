package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Reference implementation of [AuthValidator].
 *
 * Validates the requested [UnvalidatedAuthRequest], and converts it into a valid [AuthRequest].
 */
@Service
class BasicAuthValidator : AuthValidator {
    @Autowired private lateinit var clientRepository: ClientRepository

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

    override fun validateAdditionalOrError(request: UnvalidatedAuthRequest): String? {
        requireNotNull(request.clientId)

        val client = clientRepository.findById(request.clientId).get()

        if (request.responseType !in client.responseTypes)
            return "unsupported_response_type"

        request.scope?.forEach { if (it !in client.scope) return "invalid_scope" }
        if (request.scope == null) request.scope = client.scope
        return null
    }

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
