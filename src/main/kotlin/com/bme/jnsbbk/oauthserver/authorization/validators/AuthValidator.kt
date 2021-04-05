package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.client.ClientRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
interface AuthValidator {
    fun shouldRejectRequest(params: Map<String, String>, repo: ClientRepository): Optional<String>
}