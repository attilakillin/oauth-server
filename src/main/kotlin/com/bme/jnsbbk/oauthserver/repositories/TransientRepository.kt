package com.bme.jnsbbk.oauthserver.repositories

import com.bme.jnsbbk.oauthserver.authorization.AuthCode
import org.springframework.stereotype.Service

/** Repository for data that doesn't need to be persisted. Collections stored
 *  here will be deleted when the server is stopped or restarted. */
@Service
class TransientRepository {
    private val authCodes = mutableListOf<AuthCode>()

    /** Saves an authorization [code] in a transient collection. */
    fun saveAuthCode(code: AuthCode) = authCodes.add(code)
    /** Finds an authorization code with a value of [value]. Returns null if not found. */
    fun findAuthCode(value: String) = authCodes.find { code -> code.value == value }
    /** Removes the given authorization [code] from the transient collection. */
    fun removeAuthCode(code: AuthCode) = authCodes.remove(code)
}