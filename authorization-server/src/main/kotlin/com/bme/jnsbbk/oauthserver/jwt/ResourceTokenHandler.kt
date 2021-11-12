package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import org.springframework.stereotype.Service

@Service
class ResourceTokenHandler(
    private val userService: UserService,
    appConfig: AppConfig,
    rsaKeyRepository: RSAKeyRepository
) : AbstractTokenHandler(rsaKeyRepository, "resource") {
    private val lifespan = appConfig.resourceServers.userToken

    fun createToken(server: ResourceServer, user: User): String {
        return createSignedToken(server.id, lifespan) {
            setIssuer(getServerBaseUrl())
            setSubject(user.id)
            setAudience(server.id)

            return@createSignedToken this
        }
    }

    fun isTokenValid(token: String, server: ResourceServer): Boolean {
        return validateToken(token, server.id) {
            val claims = it.body

            return@validateToken claims.issuer == getServerBaseUrl()
                && claims.audience == server.id
                && userService.userExistsById(claims.subject)
        }
    }

    fun getUserFromToken(token: String, server: ResourceServer): User? {
        val jws = parseSignedToken(token, server.id) ?: return null

        return userService.getUserById(jws.body.subject)
    }
}
