package com.bme.jnsbbk.oauthserver.users.validators

import org.springframework.stereotype.Service

@Service
interface UserValidator {
    fun isRegistrationValid(email: String, password: String): Boolean
    fun isRegistrationUnique(email: String, password: String): Boolean
}