package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.UnvalidatedClient
import org.springframework.stereotype.Service

/** Base interface for client validators used in the client registration controller. */
@Service
interface ClientValidator {
    /** Validates the given [new] client as a new registration.
     *  If the validation fails, [onFailure] is called. */
    fun validateNewOrElse(new: UnvalidatedClient, onFailure: () -> Nothing): Client
    /** Validates the [new] client as an update request to the [old] client.
     *  If the validation fails, [onFailure] is called. */
    fun validateUpdateOrElse(new: UnvalidatedClient, old: Client, onFailure: () -> Nothing): Client
}