package com.bme.jnsbbk.oauthserver.repositories

import com.bme.jnsbbk.oauthserver.authorization.AuthCode
import org.springframework.stereotype.Service

@Service
class TransientRepository {
    private val authCodes = mutableListOf<AuthCode>()

    fun saveAuthCode(code: AuthCode) = authCodes.add(code)
    fun findAuthCode(value: String): AuthCode? = authCodes.find { code -> code.value == value }
}