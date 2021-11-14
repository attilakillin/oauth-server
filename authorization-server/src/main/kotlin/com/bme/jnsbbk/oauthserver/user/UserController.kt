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
     * Validates the credentials sent by the user and either logs the user in and
     * redirects them to the home page, or, upon unsuccessful registration, resends
     * the registration page with an error.
     */
    @PostMapping("/register")
    fun handleRegistration(
        @RequestParam username: String,
        @RequestParam password: String,
        @RequestParam("passwordConfirm") passwordConfirm: String,
        request: HttpServletRequest,
        model: Model
    ): String {
        val bindAttributes = { message: String ->
            model.addAttribute("username", username)
                .addAttribute("password", password)
                .addAttribute("password_confirm", passwordConfirm)
                .addAttribute("message", message)
        }

        if (userService.userExistsByUsername(username)) {
            bindAttributes("This username is taken, please choose another one!")
            return "user-register"
        }
        if (password != passwordConfirm) {
            bindAttributes("The password doesn't match the confirmation password!")
            return "user-register"
        }

        userService.createUser(username, password)
        request.logout()
        request.login(username, password)

        return "redirect:/"
    }
}
