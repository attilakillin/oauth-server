package com.bme.jnsbbk.oauthserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

@Configuration
@ConstructorBinding
@ConfigurationProperties("application")
data class AppConfig(
    /** This string appears in the iss field of every JWT. */
    var issuerString: String = "issuer_string_not_configured",
    var resourceServers: ResourceServers = ResourceServers(),
    var scheduling: Scheduling = Scheduling(),
    var tokens: Tokens = Tokens()
) {
    data class Lifespan(
        /** The offset of notBefore compared to issuedAt, in seconds. */
        val notBeforeOffset: Long = 0,
        /** The lifespan of a token, in seconds. 0 usually means it never expires. */
        val lifespan: Long = 0
    )

    data class ResourceServers(
        /** Every URL the server should accept as a valid resource server base URL must be listed here. */
        val urls: List<String> = listOf(),
        /** The lifespan of the token issued to resource servers which authenticate a specific user. */
        val userToken: Lifespan = Lifespan()
    )

    data class Scheduling(
        /** 6-digit cron string: how often expired entities should be deleted from databases? */
        val deleteExpiredEntities: String = "0 0 0 * * ?"
    )

    data class Tokens(
        /** The lifespan of OAuth authorization codes. */
        var authCode: Lifespan = Lifespan(),
        /** The lifespan of OAuth access tokens. */
        var accessToken: Lifespan = Lifespan(),
        /** The lifespan of OAuth refresh tokens. */
        var refreshToken: Lifespan = Lifespan(),
        /** The lifespan of OpenID Connect ID tokens. Must have an expiration time. */
        var idToken: Lifespan = Lifespan(lifespan = 300)
    )
}
