package com.bme.jnsbbk.oauthserver.users

import com.bme.jnsbbk.oauthserver.users.validators.UserValidator
import com.bme.jnsbbk.oauthserver.utils.PasswordHasher
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/user/register")
class UserRegistrationController (
    private val userValidator: UserValidator,
    private val userRepository: UserRepository
) {

    @GetMapping
    fun returnRegistrationForm(): String = "user_registration_form"

    @PostMapping
    fun handleRegistration(@RequestParam email: String,
                           @RequestParam password: String): String {
        if (!userValidator.isRegistrationValid(email, password)) {
            // TODO
        }

        var userId: String
        do {
            userId = RandomString.generate(16)
        } while (userRepository.existsById(userId))
        val hash = PasswordHasher.hash(password)
        val user = User(userId, email, hash)

        userRepository.save(user)
        return "user_registration_successful"
    }
}