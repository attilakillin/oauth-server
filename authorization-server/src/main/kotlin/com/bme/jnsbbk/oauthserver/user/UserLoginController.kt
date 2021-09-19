package com.bme.jnsbbk.oauthserver.user

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/user")
class UserLoginController {

    /** Sends the user login form for login requests. */
    @GetMapping("/login")
    fun serveRequest(): String = "user-login"
}
