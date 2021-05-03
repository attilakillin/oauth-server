package com.bme.jnsbbk.oauthserver.users

import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.jwt.UserJwtHandler
import com.bme.jnsbbk.oauthserver.utils.PasswordHasher
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/user")
class UserAuthenticationController(
    private val userRepository: UserRepository,
    private val jwtHandler: UserJwtHandler
) {

    /** Sends the user login form for login requests. */
    @GetMapping("/login")
    fun serveRequest(): String = "user_login_form"

    /**
     * Parses the data sent from login forms.
     *
     * Validates the credentials of the user, and returns a JWT representing this validation.
     */
    @PostMapping("/login")
    @ResponseBody
    fun handleLogin(
        @RequestParam email: String,
        @RequestParam password: String
    ): ResponseEntity<String> {
        val user = userRepository.findByEmail(email)
            ?: badRequest("Invalid credentials!")

        if (!PasswordHasher.matchesHash(password, user.passwordHash)) {
            badRequest("Invalid credentials!")
        }

        return ResponseEntity.ok(jwtHandler.createSigned(user))
    }

    /** Validates previously issued JWTs that represent a user. */
    @PostMapping("/validate")
    fun handleAuthValidation(@RequestParam userToken: String): ResponseEntity<Unit> {
        return if (jwtHandler.isUserTokenValid(userToken))
            ResponseEntity.ok().build()
        else
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }
}
