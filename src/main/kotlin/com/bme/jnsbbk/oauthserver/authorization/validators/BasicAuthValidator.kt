package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.authorization.AuthRequest
import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.stereotype.Service

@Service
class BasicAuthValidator : AuthValidator {

    override fun ifShouldReject(request: AuthRequest, repo: ClientRepository): String? {
        val client = request.clientId?.let { repo.findById(it).getOrNull() } ?: return "Unknown client!"
        val uri = request.redirectUri

        when (client.redirectUris.size) {
            1 -> if (uri != null && uri !in client.redirectUris) return "Invalid redirect URI!"
            else -> if (uri !in client.redirectUris) return "Invalid redirect URI!"
        }

        return null
    }

    override fun shouldRejectScope(scope: Set<String>, client: Client): Boolean {
        scope.forEach { if (it !in client.scope) return true }
        return false
    }
}