package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import org.springframework.stereotype.Service

/** Base interface for authorization request validation used in the authorization controller. */
@Service
interface AuthValidator {

    /**
     * Validates sensitive information.
     *
     * The expected return value of this method is null. If the method returns something else,
     * the resource owner must not be redirected to the client redirect URI!
     *
     * The validated fields in the [request] object are updated in place.
     */
    fun validateSensitiveOrError(request: UnvalidatedAuthRequest): String?

    /**
     * Validates additional, non-sensitive information.
     *
     * The expected return value of this method is null. If the method returns something else,
     * the resource owner can safely be redirected to the client redirect URI (with an error).
     *
     * Users must call the [validateSensitiveOrError] function before this. Implementations
     * should do some basic checks to ensure this, before doing additional validations.
     *
     * The validated fields in the [request] object are updated in place.
     */
    fun validateAdditionalOrError(request: UnvalidatedAuthRequest): String?

    /**
     * Converts the [request] object into a validated [AuthRequest].
     *
     * Throws an exception if any of the (otherwise required) fields are null.
     * Users must always call the two other validation functions before calling this.
     */
    fun convertToValidRequest(request: UnvalidatedAuthRequest): AuthRequest
}
