package com.bme.jnsbbk.oauthserver.resource

import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class RSService(
    private val rsRepository: RSRepository
) {

    fun authenticate(id: String, secret: String): ResourceServer? {
        val server = rsRepository.findById(id).getOrNull() ?: return null
        return if (server.secret == secret) server else null
    }

    fun authenticateBasic(authHeader: String): ResourceServer? {
        if (!authHeader.startsWith("Basic ")) return null

        val credentials = Base64.getUrlDecoder()
            .decode(authHeader.removePrefix("Basic "))
            .toString(Charsets.UTF_8)
        if (!credentials.contains(':')) return null

        val (id, secret) = credentials.split(':')
        return authenticate(id, secret)
    }
}
