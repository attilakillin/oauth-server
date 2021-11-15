package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.entities.ClientRequest
import com.bme.jnsbbk.oauthserver.exceptions.ApiException
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.utils.getIssuerString
import com.bme.jnsbbk.oauthserver.wellknown.ServerMetadata
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ServerMetadata.Endpoints.client)
class ClientRegistrationController(
    private val clientRepository: ClientRepository,
    private val clientService: ClientService
) {

    /**
     * Parses and saves a new client registration.
     *
     * Throws an [ApiException] if the requested registration is invalid.
     */
    @PostMapping
    fun registerClient(@RequestBody request: ClientRequest): ResponseEntity<Client> {
        if (!clientService.validateClientRegistration(request)) {
            badRequest("invalid_client_metadata")
        }
        val client = clientService.createValidClient(request)

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
    fun getClient(
        @RequestHeader("Authorization") header: String?,
        @PathVariable id: String
    ): ResponseEntity<Client> {
        val client = clientService.getAuthorizedClient(id, header) ?: unauthorized()
        return ResponseEntity.ok(client.withRegistrationUri())
    }

    /**
     * Deletes the given client.
     *
     * Given the right authentication, deletes the client with the given [id], or
     * throws an [ApiException] if authentication fails.
     */
    @DeleteMapping("/{id}")
    fun deleteClient(
        @RequestHeader("Authorization") header: String?,
        @PathVariable id: String
    ): ResponseEntity<String> {
        val client = clientService.getAuthorizedClient(id, header) ?: unauthorized()
        clientRepository.delete(client)
        return ResponseEntity.noContent().build()
    }

    /**
     * Updates the information of the given client.
     *
     * Given the right authentication, updates the client with the [request] values.
     * Throws an [ApiException] if authentication fails, or the request is invalid.
     */
    @PutMapping("/{id}")
    fun updateClient(
        @RequestHeader("Authorization") header: String?,
        @PathVariable id: String,
        @RequestBody request: ClientRequest
    ): ResponseEntity<Client> {
        val client = clientService.getAuthorizedClient(id, header) ?: unauthorized()

        if (!clientService.validateClientUpdate(client, request)) {
            badRequest("invalid_client_metadata")
        }
        val newClient = clientService.updateValidClient(client, request)

        clientRepository.save(newClient)
        return ResponseEntity.ok(newClient.withRegistrationUri())
    }

    /** Adds a registration management URL to the client's extra data. */
    private fun Client.withRegistrationUri(): Client {
        this.extraData["registration_client_uri"] = getIssuerString() + "/register/$id"
        return this
    }
}
