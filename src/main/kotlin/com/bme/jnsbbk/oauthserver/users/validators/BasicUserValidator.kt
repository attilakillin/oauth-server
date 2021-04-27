package com.bme.jnsbbk.oauthserver.users.validators

import com.bme.jnsbbk.oauthserver.users.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class BasicUserValidator : UserValidator {
    @Autowired private lateinit var userRepository: UserRepository

    override fun isRegistrationValid(email: String, password: String): Boolean {
        /* any char 1+ times, @, any char 1+ times, ., and 2 to 4 lowercase letters */
        val format = """^.+@.+\.[a-z]{2,4}$""".toRegex()
        /* Whitespace or multiple @ symbols */
        val illegal = """[\s]|@.*@""".toRegex()

        return email.matches(format) && !email.contains(illegal) && password.length >= 8
    }

    override fun isRegistrationUnique(email: String, password: String): Boolean {
        return !userRepository.existsById(email)
    }
}