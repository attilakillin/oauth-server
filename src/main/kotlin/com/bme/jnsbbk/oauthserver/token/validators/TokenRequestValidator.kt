package com.bme.jnsbbk.oauthserver.token.validators

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
interface TokenRequestValidator {
    fun validateClient(authHeader: String?, params: Map<String, String>, repo: ClientRepository): Optional<Client>
}