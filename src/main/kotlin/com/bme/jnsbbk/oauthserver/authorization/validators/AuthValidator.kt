package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.authorization.AuthRequest
import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import org.springframework.stereotype.Service

@Service
interface AuthValidator {
    fun ifShouldReject(request: AuthRequest, repo: ClientRepository): String?
    fun shouldRejectScope(scope: Set<String>, client: Client): Boolean
}