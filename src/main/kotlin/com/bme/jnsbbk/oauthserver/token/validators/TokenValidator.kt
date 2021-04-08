package com.bme.jnsbbk.oauthserver.token.validators

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import org.springframework.stereotype.Service

@Service
interface TokenValidator {
    fun validClientOrNull(authHeader: String?, params: Map<String, String>, repo: ClientRepository): Client?
}