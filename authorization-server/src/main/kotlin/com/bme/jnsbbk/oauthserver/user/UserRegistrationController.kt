package com.bme.jnsbbk.oauthserver.user

import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/user/register")
class UserRegistrationController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    /** Sends the registration form for registration requests. */
    @GetMapping
    fun serveRequest(): String = "user-register"

    /**
     * Parses the data sent from registration forms.
     *
     * Validates the credentials sent by the user and responds either with a HTTP 400 Bad request
     * or a 200 OK with an authentication token inside.
     */
    @PostMapping
    @ResponseBody
    fun handleRegistration(
        @RequestParam username: String,
        @RequestParam password: String
    ): ResponseEntity<Unit> {
        if (userRepository.findByUsername(username) != null) {
            badRequest("This username is taken, please choose another one!")
        }

        val id = RandomString.generateUntil(16) { !userRepository.existsById(it) }
        val user = User(id, username, passwordEncoder.encode(password))

        userRepository.save(user)
        return ResponseEntity.ok().build()
    }
}
