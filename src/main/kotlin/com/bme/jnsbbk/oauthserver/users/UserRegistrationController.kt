package com.bme.jnsbbk.oauthserver.users

import com.bme.jnsbbk.oauthserver.exceptions.ApiException
import com.bme.jnsbbk.oauthserver.exceptions.BadRequestException
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.users.validators.UserValidator
import com.bme.jnsbbk.oauthserver.utils.PasswordHasher
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/user/register")
class UserRegistrationController (
    private val userValidator: UserValidator,
    private val userRepository: UserRepository
) {

    @GetMapping
    fun returnRegistrationForm(): String = "user_registration_form"

    @PostMapping
    @ResponseBody
    fun handleRegistration(@RequestParam email: String,
                           @RequestParam password: String): ResponseEntity<Unit> {
        if (!userValidator.isRegistrationValid(email, password)) {
            badRequest("Credentials failed server-side validation. Please enter valid information!")
        }

        var userId: String
        do {
            userId = RandomString.generate(16)
        } while (userRepository.existsById(userId))
        val hash = PasswordHasher.hash(password)
        val user = User(userId, email, hash)

        userRepository.save(user)
        return ResponseEntity.ok().build()
    }

}