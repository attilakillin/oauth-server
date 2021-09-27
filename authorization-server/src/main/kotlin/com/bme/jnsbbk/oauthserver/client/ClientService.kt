package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.utils.decodeAsHttpBasic
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.stereotype.Service

@Service
class ClientService(
    private val clientRepository: ClientRepository
) {
    /** Authenticate a client based on its ID and secret. Returns null if unauthorized. */
    fun authenticate(id: String, secret: String): Client? {
        val client = clientRepository.findById(id).getOrNull() ?: return null
        return if (client.secret == secret) client else null
    }

    /** Authenticate a client using HTTP Basic authentication. Returns null if unauthorized. */
    fun authenticateBasic(authHeader: String): Client? {
        val (id, secret) = authHeader.decodeAsHttpBasic() ?: return null
        return authenticate(id, secret)
    }

    /** Authenticate a client using parameters given with a String Map. Returns null if unauthorized. */
    fun authenticateParam(parameters: Map<String, String>): Client? {
        val id = parameters["client_id"]
        val secret = parameters["client_secret"]
        if (id == null || secret == null) return null
        return authenticate(id, secret)
    }

    /**
     * Authenticate a client with either HTTP Basic or request parameter authentication.
     *
     * Returns null if unauthorized, or if both methods are used simultaneously.
     */
    fun authenticateWithEither(authHeader: String?, parameters: Map<String, String>): Client? {
        val client1 = if (authHeader != null) authenticateBasic(authHeader) else null
        val client2 = authenticateParam(parameters)
        return if (client1 != null) {
            if (client2 == null) client1 else null // If both methods were used simultaneously, return null.
        } else {
            client2
        }
    }
}
