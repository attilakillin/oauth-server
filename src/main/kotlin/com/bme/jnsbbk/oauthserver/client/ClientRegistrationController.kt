package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.validators.ClientValidator
import com.bme.jnsbbk.oauthserver.exceptions.ApiException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/register")
class ClientRegistrationController(
    private val clientValidator: ClientValidator
) {

    @PostMapping("")
    fun registerClient(@RequestBody client: Client): ResponseEntity<Client> {
        if (clientValidator.shouldReject(client))
            throw ApiException(HttpStatus.BAD_REQUEST, "invalid_client_metadata")

        clientValidator.parseAndValidate(client)
        return ResponseEntity.ok(client)
    }
}