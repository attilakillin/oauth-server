package com.bme.jnsbbk.oauthserver.resource

import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.bme.jnsbbk.oauthserver.utils.decodeAsHttpBasic
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.stereotype.Service

@Service
class ResourceServerService(
    private val resourceServerRepository: ResourceServerRepository
) {
    /** Authenticate a resource server based on its ID and secret. Returns null if unauthorized. */
    fun authenticate(id: String, secret: String): ResourceServer? {
        val server = resourceServerRepository.findById(id).getOrNull() ?: return null
        return if (server.secret == secret) server else null
    }

    /** Authenticate a resource server using HTTP Basic authentication. Returns null if unauthorized. */
    fun authenticateBasic(authHeader: String?): ResourceServer? {
        if (authHeader == null) return null
        val (id, secret) = authHeader.decodeAsHttpBasic() ?: return null
        return authenticate(id, secret)
    }

    /** Returns true or false depending on whether a resource server with the given URL exists or not. */
    fun resourceServerExists(url: String): Boolean = resourceServerRepository.findByUrl(url) != null

    /** Creates and persists a resource server with the given parameters. */
    fun createResourceServer(url: String, scope: Set<String>): ResourceServer {
        val id = RandomString.generateUntil { !resourceServerRepository.existsById(it) }
        val secret = RandomString.generate()
        val rs = ResourceServer(id, secret, url, scope)
        return resourceServerRepository.save(rs)
    }
}
