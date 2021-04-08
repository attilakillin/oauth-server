package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import org.springframework.stereotype.Service

@Service
interface ClientValidator {
    fun shouldRejectCreation(client: Client): Boolean
    fun validateCreationValues(client: Client, repo: ClientRepository)

    fun shouldRejectUpdate(old: Client, new: Client): Boolean
    fun validateUpdateValues(old: Client, new: Client)
}