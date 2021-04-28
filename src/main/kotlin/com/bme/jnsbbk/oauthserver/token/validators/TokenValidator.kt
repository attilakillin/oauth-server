package com.bme.jnsbbk.oauthserver.token.validators

import com.bme.jnsbbk.oauthserver.client.entities.Client
import org.springframework.stereotype.Service

@Service
interface TokenValidator {
    fun validClientOrNull(authHeader: String?, params: Map<String, String>): Client?
}