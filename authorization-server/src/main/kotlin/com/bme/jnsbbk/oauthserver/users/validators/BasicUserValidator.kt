package com.bme.jnsbbk.oauthserver.users.validators

import org.springframework.stereotype.Service

/** Reference implementation of [UserValidator]. */
@Service
class BasicUserValidator : UserValidator {

    override fun isRegistrationValid(email: String, password: String): Boolean {
        val format = """^.+@.+$""".toRegex() // A '@' with at least 1 character on each side
        val illegal = """[\s]|@.*@""".toRegex() // Multiple '@' symbols, or whitespace

        return email.matches(format) && !email.contains(illegal) && password.length >= 8
    }
}
