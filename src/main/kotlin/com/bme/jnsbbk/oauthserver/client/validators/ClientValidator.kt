package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.entities.UnvalidatedClient
import org.springframework.stereotype.Service

/** Base interface for client validators used in the client registration controller. */
@Service
interface ClientValidator {

    /**
     * Validates the given [new] client as a new registration.
     *
     * If validation succeeds, returns a valid client with the values specified in
     * the [UnvalidatedClient]. If it fails, [onFailure] is called.
     *
     * [onFailure] should be a lambda that returns [Nothing] aka it should always
     * throw an exception.
     */
    fun validateNewOrElse(new: UnvalidatedClient, onFailure: () -> Nothing): Client

    /**
     * Validates the [new] client as an update request to the [old] client.
     *
     * If validation succeeds, returns a valid client with the values specified in
     * the [UnvalidatedClient]. If it fails, [onFailure] is called.
     *
     * [onFailure] should be a lambda that returns [Nothing] aka it should always
     * throw an exception.
     */
    fun validateUpdateOrElse(new: UnvalidatedClient, old: Client, onFailure: () -> Nothing): Client
}