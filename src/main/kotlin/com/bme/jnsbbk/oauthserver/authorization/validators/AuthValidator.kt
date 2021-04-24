package com.bme.jnsbbk.oauthserver.authorization.validators

import com.bme.jnsbbk.oauthserver.authorization.AuthRequest
import com.bme.jnsbbk.oauthserver.authorization.UnvalidatedAuthRequest
import org.springframework.stereotype.Service

/** Base interface for authorization request validation used in the authorization controller. */
@Service
interface AuthValidator {
    /** A complex validator method. Validates the given [request] and calls [success] if the
     *  validation succeeded. If the validation failed, and it was an authentication failure
     *  that should not result in a redirect, [errorIfNoRedirect] is called. If it was an otherwise
     *  minor error that can be safely communicated back to the client, [errorIfRedirect] is called.
     *
     *  The return value is the same as the return value of the lambda that was called during validation. */
    fun validate(request: UnvalidatedAuthRequest,
                 errorIfNoRedirect: (String) -> String,
                 errorIfRedirect: (String, String) -> String,
                 success: (AuthRequest) -> String): String
}