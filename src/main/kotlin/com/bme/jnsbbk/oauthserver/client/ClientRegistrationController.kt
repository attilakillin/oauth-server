package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.validators.ClientValidator
import com.bme.jnsbbk.oauthserver.exceptions.ApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    fun getClient(@RequestHeader("Authorization") accessToken: String?,
                  @PathVariable id: String): ResponseEntity<Client> {
        if (accessToken.isNullOrEmpty() || !accessToken.startsWith("Bearer "))
            return ResponseEntity(HttpStatus.UNAUTHORIZED)

        val token = accessToken.removePrefix("Bearer ")
        val result = clientRepository.findById(id)
        if (result.isEmpty || result.get().registrationAccessToken != token)
            return ResponseEntity(HttpStatus.UNAUTHORIZED)

        return ResponseEntity.ok(result.get())
    }
}