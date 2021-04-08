package com.bme.jnsbbk.oauthserver.repositories

import com.bme.jnsbbk.oauthserver.authorization.AuthorizationCode
import org.springframework.stereotype.Service

@Service
class TransientRepository {
    private val authorizationCodes = mutableListOf<AuthorizationCode>()

    fun saveAuthorizationCode(value: String, properties: Map<String, String>, lifeInSeconds: Long) =
        authorizationCodes.add(AuthorizationCode(value, properties, lifeInSeconds))

    fun findAuthorizationCode(value: String): AuthorizationCode? =
        authorizationCodes.find { code -> code.value == value }
}