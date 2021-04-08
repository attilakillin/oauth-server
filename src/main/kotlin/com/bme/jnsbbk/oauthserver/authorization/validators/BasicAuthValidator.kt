package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class BasicAuthValidator : AuthValidator {

    override fun ifShouldReject(params: Map<String, String>, repo: ClientRepository): String? {
        var client: Client? = null
        val id = params["client_id"]
        if (id != null) client = repo.findById(id).getOrNull()

        if (client == null) return "Unknown client!"
        val redirectUri = params["redirect_uri"]

        when (client.redirectUris.size) {
            1 -> if (redirectUri != null && redirectUri !in client.redirectUris)
                return "Invalid redirect URI!"
            else -> if (redirectUri !in client.redirectUris)
                return "Invalid redirect URI!"
        }

        return null
    }

    override fun shouldRejectScope(scope: Set<String>, client: Client): Boolean {
        scope.forEach { if (it !in client.scope) return true }
        return false
    }

    private fun <T> Optional<T>.getOrNull(): T? = if (isPresent) get() else null
}