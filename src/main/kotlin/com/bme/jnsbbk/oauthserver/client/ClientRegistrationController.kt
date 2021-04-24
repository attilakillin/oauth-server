package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.validators.ClientValidator
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/register")
class ClientRegistrationController (
    private val clientValidator: ClientValidator,
    private val clientRepository: ClientRepository
) {

    /** Parses and saves a new client registration. Returns with the saved value. */
    @PostMapping
    fun registerClient(@RequestBody requested: UnvalidatedClient): ResponseEntity<Client> {
        val client = clientValidator.validateNewOrElse(requested) {
            badRequest("invalid_client_metadata")
        }

        clientRepository.save(client)
        return ResponseEntity.ok(client.withRegistrationUri())
    }

    /** Given the right authentication, returns everything stored about the client with the given [id]. */
    @GetMapping("/{id}")
    fun getClient(@RequestHeader("Authorization") header: String?,
                  @PathVariable id: String): ResponseEntity<Client> {
        val client = validClientOrUnauthorized(header, id)
        return ResponseEntity.ok(client.withRegistrationUri())
    }

    /** Given the right authentication, deletes the client with the given [id]. */
    @DeleteMapping("/{id}")
    fun deleteClient(@RequestHeader("Authorization") header: String?,
                     @PathVariable id: String): ResponseEntity<String> {
        val client = validClientOrUnauthorized(header, id)
        clientRepository.delete(client)
        return ResponseEntity.noContent().build()
    }

    /** Given the right authentication, updates the client with the given [id] with the [requested] values. */
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

    /** Processes the given authentication credentials. Returns a valid client if the
     *  credentials match the expected values, or throws an UnauthorizedException otherwise. */
    private fun validClientOrUnauthorized(header: String?, id: String): Client {
        val token = header?.removePrefix("Bearer ")
        if (token != header) {
            val client = clientRepository.findById(id).getOrNull()
            if (client != null && client.registrationAccessToken == token)
                return client
        }

        unauthorized()
    }

    /** Extension function, appends the registration client URI to the client's extra data. */
    private fun Client.withRegistrationUri(): Client {
        val url = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()
        this.extraData["registration_client_uri"] = "$url/register/$id"
        return this
    }
}