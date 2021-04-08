package com.bme.jnsbbk.oauthserver.token.validators

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class BasicTokenValidator : TokenValidator {
    override fun validClientOrNull(authHeader: String?, params: Map<String, String>,
                                   repo: ClientRepository): Client? {
        var clientId: String? = null
        var clientSecret: String? = null

        if (authHeader != null && authHeader.startsWith("Basic ")) {
            val content = Base64.getUrlDecoder().decode(authHeader.removePrefix("Basic ")).toString()
            clientId = content.substringBefore(':')
            clientSecret = content.substringAfter(':')
        }

        if ("client_id" in params.keys) {
            if (clientId != null) return null
            clientId = params["client_id"]
            clientSecret = params["client_secret"]
        }

        if (clientId == null || clientSecret == null) return null

        val client = repo.findById(clientId)
        if (client.isEmpty || client.get().secret != clientSecret) return null

        return client.get()
    }
}