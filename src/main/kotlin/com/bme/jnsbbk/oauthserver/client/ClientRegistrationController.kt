package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.validators.ClientValidator
import com.bme.jnsbbk.oauthserver.exceptions.ApiException
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/register")
class ClientRegistrationController (
    private val clientValidator: ClientValidator,
    private val clientRepository: ClientRepository
) {

    @PostMapping("")
    fun registerClient(@RequestBody client: Client): ResponseEntity<Client> {
        if (clientValidator.shouldRejectCreation(client))
            throw ApiException(HttpStatus.BAD_REQUEST, "invalid_client_metadata")

        clientValidator.validateCreationValues(client, clientRepository)
        clientRepository.save(client)

        return ResponseEntity.ok(client.withRegistrationUri())
    }

    @GetMapping("/{id}")
    fun getClient(@RequestHeader("Authorization") header: String?,
                  @PathVariable id: String): ResponseEntity<Client> {
        val client = validClientOrThrow(header, id, HttpStatus.UNAUTHORIZED)
        return ResponseEntity.ok(client.withRegistrationUri())
    }

    @DeleteMapping("/{id}")
    fun deleteClient(@RequestHeader("Authorization") header: String?,
                     @PathVariable id: String): ResponseEntity<String> {
        val client = validClientOrThrow(header, id, HttpStatus.UNAUTHORIZED)

        clientRepository.delete(client)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{id}")
    fun updateClient(@RequestHeader("Authorization") header: String?,
                     @PathVariable id: String,
                     @RequestBody newClient: Client): ResponseEntity<Client> {
        val oldClient = validClientOrThrow(header, id, HttpStatus.UNAUTHORIZED)

        if (clientValidator.shouldRejectUpdate(oldClient, newClient))
            throw ApiException(HttpStatus.BAD_REQUEST, "invalid_client_metadata")

        clientValidator.validateUpdateValues(oldClient, newClient)
        clientRepository.save(newClient)

        return ResponseEntity.ok(newClient.withRegistrationUri())
    }

    private fun validClientOrThrow(header: String?, id: String, error: HttpStatus): Client {
        val token = header?.removePrefix("Bearer ")
        if (token != header) {
            val client = clientRepository.findById(id).getOrNull()
            if (client != null && client.registrationAccessToken == token)
                return client
        }

        throw ApiException(error)
    }

    private fun Client.withRegistrationUri(): Client {
        val url = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString()
        this.extraInfo["registration_client_uri"] = "$url/register/$id"
        return this
    }
}