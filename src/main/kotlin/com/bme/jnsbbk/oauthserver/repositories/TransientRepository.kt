package com.bme.jnsbbk.oauthserver.repositories

import org.springframework.stereotype.Service

@Service
class TransientRepository {
    val authCodes = mutableMapOf<String, Map<String, String>>()
}