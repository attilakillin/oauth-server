package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import org.springframework.stereotype.Service

@Service
interface AuthValidator {
    fun ifShouldReject(params: Map<String, String>, repo: ClientRepository): String?
    fun shouldRejectScope(scope: Set<String>, client: Client): Boolean
}