package com.bme.jnsbbk.resourceserver.authentication

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AuthenticationController {

    @GetMapping("/login-error")
    fun handleLoginError(): String = "login-error"
}
