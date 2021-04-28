package com.bme.jnsbbk.oauthserver.token.validators

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class BasicTokenValidator : TokenValidator {
    @Autowired private lateinit var clientRepository: ClientRepository

    override fun validClientOrNull(authHeader: String?, params: Map<String, String>): Client? {
        var id: String? = null
        var secret: String? = null

        if (authHeader != null && authHeader.startsWith("Basic ")) {
            val content = Base64.getUrlDecoder().decode(authHeader.removePrefix("Basic ")).toString()
            id = content.substringBefore(':')
            secret = content.substringAfter(':')
        }

        if ("client_id" in params.keys) {
            if (id != null) return null
            id = params["client_id"]
            secret = params["client_secret"]
        }

        if (id == null) return null

        val client = clientRepository.findById(id).getOrNull()
        if (client == null || client.secret != secret) return null

        return client
    }
}