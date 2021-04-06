package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.client.ClientRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class BasicAuthValidator : AuthValidator {

    override fun shouldRejectRequest(params: Map<String, String>, repo: ClientRepository): Optional<String> {
        if (params["client_id"] == null) return Optional.of("Unknown client!")
        val clientWrap = repo.findById(params["client_id"]!!)
        if (clientWrap.isEmpty) return Optional.of("Unknown client!")

        val client = clientWrap.get()
        val redirectUri = params["redirect_uri"]

        when (client.redirectUris.size) {
            1 -> if (redirectUri != null && redirectUri !in client.redirectUris)
                return Optional.of("Invalid redirect URI!")
            else -> if (redirectUri !in client.redirectUris)
                return Optional.of("Invalid redirect URI!")
        }

        return Optional.empty()
    }
}