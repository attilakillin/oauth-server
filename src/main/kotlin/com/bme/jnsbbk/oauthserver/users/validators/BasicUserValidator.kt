package com.bme.jnsbbk.oauthserver.users.validators

import org.springframework.stereotype.Service

@Service
class BasicUserValidator : UserValidator {
    override fun isRegistrationValid(email: String, password: String): Boolean {
        /* any char 1+ times, @, any char 1+ times, ., and 2 to 4 lowercase letters */
        val format = """^.+@.+\.[a-z]{2,4}$""".toRegex()
        /* Whitespace or multiple @ symbols */
        val illegal = """[\s]|@.*@""".toRegex()

        return email.matches(format) && !email.contains(illegal) && password.length >= 8
    }
}