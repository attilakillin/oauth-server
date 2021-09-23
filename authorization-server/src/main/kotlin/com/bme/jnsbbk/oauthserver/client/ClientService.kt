package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class ClientService(
    private val clientRepository: ClientRepository
) {

    fun authenticate(id: String, secret: String): Client? {
        val client = clientRepository.findById(id).getOrNull() ?: return null
        return if (client.secret == secret) client else null
    }

    fun authenticateBasic(authHeader: String): Client? {
        if (!authHeader.startsWith("Basic ")) return null

        val credentials = Base64.getUrlDecoder()
            .decode(authHeader.removePrefix("Basic "))
            .toString(Charsets.UTF_8)
        if (!credentials.contains(':')) return null

        val (id, secret) = credentials.split(':')
        return authenticate(id, secret)
    }

    fun authenticateParam(parameters: Map<String, String>): Client? {
        val id = parameters["client_id"]
        val secret = parameters["client_secret"]
        if (id == null || secret == null) return null
        return authenticate(id, secret)
    }

    fun authenticateWithEither(authHeader: String?, parameters: Map<String, String>): Client? {
        val client1 = if (authHeader != null) authenticateBasic(authHeader) else null
        val client2 = authenticateParam(parameters)
        return if (client1 != null) {
            if (client2 == null) client1 else null  // If both methods were used simultaneously, return null.
        } else {
            client2
        }
    }
}
