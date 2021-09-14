package com.bme.jnsbbk.oauthserver.users.validators

import org.springframework.stereotype.Service

/** Base interface for user validation used in the user registration controller. */
@Service
interface UserValidator {

    /**
     * Checks whether the specified credentials represent a valid registration.
     *
     * Different implementations can specify different constraints for either
     * the email or the password.
     */
    fun isRegistrationValid(email: String, password: String): Boolean
}
