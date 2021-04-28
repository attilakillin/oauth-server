package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import org.springframework.stereotype.Service

/** Base interface for authorization request validation used in the authorization controller. */
@Service
interface AuthValidator {

    /** Validates sensitive information. The expected return value of this method is null.
     *  If the method returns something else, the resource owner must not be redirected to
     *  the client redirect URI!
     *
     *  Validated fields are updated in the [request] object. */
    fun validateSensitiveOrError(request: UnvalidatedAuthRequest): String?

    /** Validates additional information. The method requires that the [validateSensitiveOrError]
     *  method be called before this, otherwise it can throw exceptions.
     *
     *  The expected return value of this method is null. If a string is returned, the resource
     *  owner can safely be redirected to the client redirect URI (with an error).
     *
     *  Validated fields are updated in the [request] object. */
    fun validateAdditionalOrError(request: UnvalidatedAuthRequest): String?

    /** Converts the [request] object into a validated [AuthRequest].
     *  Throws an exception if any of the (otherwise required) fields are null.
     *  To avoid these exceptions, always call the two validation methods before calling this. */
    fun convertToValidRequest(request: UnvalidatedAuthRequest): AuthRequest
}