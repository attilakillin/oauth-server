package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.validators.ClientValidator
import com.bme.jnsbbk.oauthserver.exceptions.ApiException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/register")
class ClientRegistrationController(
    private val clientValidator: ClientValidator,
    private val clientRepository: ClientRepository
) {

    @PostMapping("")
    fun registerClient(@RequestBody client: Client): ResponseEntity<Client> {
        if (clientValidator.shouldReject(client))
            throw ApiException(HttpStatus.BAD_REQUEST, "invalid_client_metadata")

        clientValidator.parseAndValidate(client)
        clientRepository.save(client)
        return ResponseEntity.ok(client)
    }

    @GetMapping("/{id}")
    fun getClient(@RequestHeader("Authorization") header: String?,
                  @PathVariable id: String): ResponseEntity<Client> {
        val result = validateHeaderAndReturnClient(header, id)
        if (result.isEmpty)
            return ResponseEntity(HttpStatus.UNAUTHORIZED)

        return ResponseEntity.ok(result.get())
    }

    @DeleteMapping("/{id}")
    fun deleteClient(@RequestHeader("Authorization") header: String?,
                     @PathVariable id: String): ResponseEntity<String> {
        val result = validateHeaderAndReturnClient(header, id)
        if (result.isEmpty)
            return ResponseEntity(HttpStatus.UNAUTHORIZED)

        clientRepository.delete(result.get())
        return ResponseEntity.noContent().build()
    }

    private fun validateHeaderAndReturnClient(header: String?, id: String): Optional<Client> {
        if (header.isNullOrEmpty() || !header.startsWith("Bearer "))
            return Optional.empty()

        val token = header.removePrefix("Bearer ")
        if (token.isEmpty())
            return Optional.empty()

        val result = clientRepository.findById(id)
        if (result.isEmpty || result.get().registrationAccessToken != token)
            return Optional.empty()

        return Optional.of(result.get())
    }
}