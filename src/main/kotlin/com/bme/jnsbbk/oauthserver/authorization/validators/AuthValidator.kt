package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.authorization.AuthRequest
import com.bme.jnsbbk.oauthserver.authorization.UnvalidatedAuthRequest
import org.springframework.stereotype.Service

/** Base interface for authorization request validation used in the authorization controller. */
@Service
interface AuthValidator {
    /** Validates sensitive information. The expected return value of this method is null.
     *  If the method returns something else, caution should be used, and the resource owner
     *  must not be redirected to the client redirect URI!
     *
     *  Validated fields are updated in the [request] object. */
    fun validateSensitiveOrError(request: UnvalidatedAuthRequest): String?
    /** Validates additional information. The method requires that the [validateSensitiveOrError]
     *  method be called before this, otherwise it can throw an error.
     *
     *  The expected return value of this method is null. If a string is returned, the resource
     *  owner can safely be redirected to the client redirect URI.
     *
     *  Validated fields are updated in the [request] object. */
    fun validateAdditionalOrError(request: UnvalidatedAuthRequest): String?
    /** Converts the [request] object into a validated [AuthRequest].
     *  Throws an error, if the validation methods were not used beforehand and either (otherwise
     *  required) field is null. */
    fun convertToValidRequest(request: UnvalidatedAuthRequest): AuthRequest
}