package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.entities.UnvalidatedClient
import com.bme.jnsbbk.oauthserver.client.validators.ClientValidator
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.exceptions.ApiException
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/register")
class ClientRegistrationController (
    private val clientValidator: ClientValidator,
    private val clientRepository: ClientRepository
) {

    /**
     * Parses and saves a new client registration.
     *
     * Throws an [ApiException] if the requested registration is invalid.
     */
    @PostMapping
    fun registerClient(@RequestBody requested: UnvalidatedClient): ResponseEntity<Client> {
        val client = clientValidator.validateNewOrElse(requested) {
            badRequest("invalid_client_metadata")
        }

        clientRepository.save(client)
        return ResponseEntity.ok(client.withRegistrationUri())
    }

    /**
     * Returns every stored information about a given client.
     *
     * Given the right authentication, returns everything stored about the client
     * with the given [id]. Throws an [ApiException] if authentication fails.
     */
    @GetMapping("/{id}")
    fun getClient(@RequestHeader("Authorization") header: String?,
                  @PathVariable id: String): ResponseEntity<Client> {
        val client = validClientOrUnauthorized(header, id)
        return ResponseEntity.ok(client.withRegistrationUri())
    }

    /**
     * Deletes the given client.
     *
     * Given the right authentication, deletes the client with the given [id], or
     * throws an [ApiException] if authentication fails.
     */
    @DeleteMapping("/{id}")
    fun deleteClient(@RequestHeader("Authorization") header: String?,
                     @PathVariable id: String): ResponseEntity<String> {
        val client = validClientOrUnauthorized(header, id)
        clientRepository.delete(client)
        return ResponseEntity.noContent().build()
    }

    /**
     * Updates the information of the given client.
     *
     * Given the right authentication, updates the client with the [requested] values.
     * Throws an [ApiException] if authentication fails, or the request is invalid.
     */
    @PutMapping("/{id}")
    fun updateClient(@RequestHeader("Authorization") header: String?,
                     @PathVariable id: String,
                     @RequestBody requested: UnvalidatedClient): ResponseEntity<Client> {
        val oldClient = validClientOrUnauthorized(header, id)
        val newClient = clientValidator.validateUpdateOrElse(requested, oldClient) {
            badRequest("invalid_client_metadata")
        }

        clientRepository.save(newClient)
        return ResponseEntity.ok(newClient.withRegistrationUri())
    }

    private fun Client.withRegistrationUri(): Client {
        this.extraData["registration_client_uri"] = getServerBaseUrl() + "/register/$id"
        return this
    }

    /** Validates the authentication [header] for the given client [id]. */
    private fun validClientOrUnauthorized(header: String?, id: String): Client {
        val token = header?.removePrefix("Bearer ")
        if (token != header) {
            val client = clientRepository.findById(id).getOrNull()
            if (client != null && client.registrationAccessToken == token)
                return client
        }

        unauthorized()
    }
}