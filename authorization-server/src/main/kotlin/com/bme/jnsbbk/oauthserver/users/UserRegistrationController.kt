package com.bme.jnsbbk.oauthserver.users

import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.jwt.UserJwtHandler
import com.bme.jnsbbk.oauthserver.users.entities.User
import com.bme.jnsbbk.oauthserver.users.validators.UserValidator
import com.bme.jnsbbk.oauthserver.utils.PasswordHasher
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/user/register")
class UserRegistrationController(
    private val userValidator: UserValidator,
    private val userRepository: UserRepository,
    private val jwtHandler: UserJwtHandler
) {

    /** Sends the registration form for registration request. */
    @GetMapping
    fun serveRequest(): String = "user_registration_form"

    /**
     * Parses the data sent from registration forms.
     *
     * Validates the credentials sent by the user and responds either with a HTTP 400 Bad request
     * or a 200 OK with an authentication token inside.
     */
    @PostMapping
    @ResponseBody
    fun handleRegistration(
        @RequestParam email: String,
        @RequestParam password: String
    ): ResponseEntity<String> {
        if (!userValidator.isRegistrationValid(email, password)) {
            badRequest("Credentials failed server-side validation. Please enter valid information!")
        }
        if (userRepository.existsById(email)) {
            badRequest("This email is already registered!")
        }

        val id = RandomString.generateUntil(16) { !userRepository.existsById(it) }
        val hash = PasswordHasher.hash(password)
        val user = User(id, email, hash)

        userRepository.save(user)
        return ResponseEntity.ok(jwtHandler.createSigned(user))
    }
}
