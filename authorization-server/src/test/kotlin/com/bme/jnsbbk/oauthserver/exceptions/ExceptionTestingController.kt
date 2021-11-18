package com.bme.jnsbbk.oauthserver.exceptions

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ExceptionTestingController {

    @GetMapping("/throw/400/{message}")
    fun throwBadRequestWithMessage(@PathVariable message: String): Nothing = badRequest(message)

    @GetMapping("/throw/401/{message}")
    fun throwUnauthorizedWithMessage(@PathVariable message: String): Nothing = unauthorized(message)
}
