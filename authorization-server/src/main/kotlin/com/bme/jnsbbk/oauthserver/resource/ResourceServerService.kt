package com.bme.jnsbbk.oauthserver.resource

import com.bme.jnsbbk.oauthserver.jwt.ResourceTokenHandler
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.bme.jnsbbk.oauthserver.utils.decodeAsHttpBasic
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*

@Service
class ResourceServerService(
    private val resourceServerRepository: ResourceServerRepository,
    private val resourceTokenHandler: ResourceTokenHandler
) {
    /** Authenticate a resource server based on its ID and secret. Returns null if unauthorized. */
    fun authenticate(id: String, secret: String): ResourceServer? {
        val server = resourceServerRepository.findByIdOrNull(id) ?: return null
        return if (server.secret == secret) server else null
    }

    /** Authenticate a resource server using HTTP Basic authentication. Returns null if unauthorized. */
    fun authenticateBasic(authHeader: String?): ResourceServer? {
        if (authHeader == null) return null
        val (id, secret) = authHeader.decodeAsHttpBasic() ?: return null
        return authenticate(id, secret)
    }

    /** Returns true or false depending on whether a resource server with the given URL exists or not. */
    fun serverExistsByUrl(url: String): Boolean {
        return resourceServerRepository.findByBaseUrl(url) != null
    }

    /** Shorthand function that returns a resource server by its ID, or null, if no such server exists. */
    fun getServerById(id: String?): ResourceServer? {
        return if (id != null) resourceServerRepository.findByIdOrNull(id) else null
    }

    /** Creates and persists a resource server with the given base URL and scope set. */
    fun createServer(url: String, scope: Set<String>): ResourceServer {
        val id = RandomString.generateUntil { !resourceServerRepository.existsById(it) }
        val secret = RandomString.generate()
        val server = ResourceServer(id, secret, url, scope)
        return resourceServerRepository.save(server)
    }

    /** Creates and encodes a user authentication token in the context of the resource server. */
    fun createEncodedUserToken(server: ResourceServer, user: User): String {
        val token = resourceTokenHandler.createToken(server, user)
        return Base64.getUrlEncoder().encodeToString(token.toByteArray(Charsets.UTF_8))
    }

    /** Returns whether the given user token is valid in the context of the resource server. */
    fun isUserTokenValid(server: ResourceServer, token: String): Boolean {
        return resourceTokenHandler.isTokenValid(token, server)
    }

    /** Extracts the user contained in the given user token in the context of the resource server. */
    fun getUserFromUserToken(server: ResourceServer, token: String): User? {
        return resourceTokenHandler.getUserFromToken(token, server)
    }
}
