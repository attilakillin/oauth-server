package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.client.ClientRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class BasicAuthValidator : AuthValidator {

    override fun shouldRejectRequest(clientId: String, redirectUri: String,
                                     repo: ClientRepository): Optional<String> {
        val client = repo.findById(clientId)
        if (client.isEmpty)
            return Optional.of("Unknown client!")

        if (redirectUri !in client.get().redirectUris)
            return Optional.of("Invalid redirect URI!")

        return Optional.empty()
    }
}