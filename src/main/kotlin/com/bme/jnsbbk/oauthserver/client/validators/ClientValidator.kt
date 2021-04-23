package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.Client
import org.springframework.stereotype.Service

@Service
interface ClientValidator {
    fun validateNewOr(client: Client, onFailure: () -> Nothing): Client
    fun validateUpdateOr(old: Client, new: Client, onFailure: () -> Nothing): Client
}