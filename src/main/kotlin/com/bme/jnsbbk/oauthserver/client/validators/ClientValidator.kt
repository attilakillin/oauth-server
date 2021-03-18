package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.Client
import org.springframework.stereotype.Service

@Service
interface ClientValidator {
    fun shouldReject(client: Client): Boolean
    fun parseAndValidate(client: Client)
}