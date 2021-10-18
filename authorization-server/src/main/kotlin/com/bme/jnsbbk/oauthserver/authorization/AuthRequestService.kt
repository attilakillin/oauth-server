package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.stereotype.Service

@Service
class AuthRequestService(
    private val clientRepository: ClientRepository
) {

    fun isSensitiveInfoValid(request: UnvalidatedAuthRequest): Pair<Boolean, String> {
        val client = request.clientId?.let { clientRepository.findById(it).getOrNull() }
            ?: return Pair(false, "Unknown client!")

        val redirectMissing = if (client.redirectUris.size == 1) {
            request.redirectUri != null && request.redirectUri != client.redirectUris.first()
        } else {
            request.redirectUri !in client.redirectUris
        }

        return if (redirectMissing) Pair(false, "Invalid redirect URI!") else Pair(true, "")
    }

    fun isAdditionalInfoValid(request: UnvalidatedAuthRequest): Pair<Boolean, String> {
        requireNotNull(request.clientId) // Calling this function without verifying sensitive info may throw an error
        val client = clientRepository.findById(request.clientId).get()

        if (request.responseType !in client.responseTypes) return Pair(false, "unsupported_response_type")
        request.scope?.forEach { if (it !in client.scope) return Pair(false, "invalid_scope") }
        return Pair(true, "")
    }

    fun convertToValidRequest(request: UnvalidatedAuthRequest): AuthRequest {
        requireNotNull(request.clientId) // Same thing here, only call this after the other two functions
        val client = clientRepository.findById(request.clientId).get()

        return AuthRequest(
            clientId = request.clientId,
            redirectUri = request.redirectUri ?: client.redirectUris.first(),
            responseType = request.responseType!!,
            scope = request.scope ?: client.scope,
            state = request.state,
            nonce = request.nonce
        )
    }
}
