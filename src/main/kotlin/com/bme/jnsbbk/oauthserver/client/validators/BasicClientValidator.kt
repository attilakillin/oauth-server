package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.Client
import org.springframework.stereotype.Service

@Service
class BasicClientValidator : ClientValidator {

    override fun shouldReject(client: Client): Boolean {
        if (client.id != null || client.secret != null)
            return true
        if (client.redirectUris.isEmpty())
            return true
        return false
    }

    override fun parseAndValidate(client: Client) {

    }
}