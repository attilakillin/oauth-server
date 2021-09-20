package com.bme.jnsbbk.oauthserver.user

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/user")
class UserController(
    private val userService: UserService
) {

    /** Sends the user login form for login requests. */
    @GetMapping("/login")
    fun onLoginRequest(): String = "user-login"

    /** Sends the registration form for registration requests. */
    @GetMapping("/register")
    fun onRegisterRequest(): String = "user-register"

    /**
     * Parses the data sent from registration forms.
     *
     * Validates the credentials sent by the user and responds either with a HTTP 400 Bad request
     * or a 200 OK with an authentication token inside.
     */
    @PostMapping("/register")
    fun handleRegistration(
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam password_confirm: String,
        request: HttpServletRequest,
        model: Model
    ): String {
        val bindAttributes = { message: String ->
            model.addAttribute("username", username)
                .addAttribute("password", password)
                .addAttribute("password_confirm", password_confirm)
                .addAttribute("message", message)
        }

        if (userService.userExists(username)) {
            bindAttributes("This username is taken, please choose another one!")
            return "user-register"
        }
        if (password != password_confirm) {
            bindAttributes("The password doesn't match the confirmation password!")
            return "user-register"
        }

        userService.createUser(username, password)
        request.login(username, password)

        return "redirect:/"
    }
}
