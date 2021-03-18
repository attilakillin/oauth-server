package com.bme.jnsbbk.oauthserver.controllers

import com.bme.jnsbbk.oauthserver.model.Client
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/register")
class ClientRegistrationController {

    @PostMapping("")
    fun registerClient(@RequestBody body: Client): ResponseEntity<Client> {
        return ResponseEntity.ok(body)
    }
}