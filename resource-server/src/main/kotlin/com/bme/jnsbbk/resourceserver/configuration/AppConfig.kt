package com.bme.jnsbbk.resourceserver.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry

@Configuration
@EnableRetry
@ConstructorBinding
@ConfigurationProperties("application")
data class AppConfig(
    var authorizationServer: AuthorizationServer = AuthorizationServer()
) {
    data class Endpoints(
        /** This endpoint is used for registering the resource server at the authorization server. */
        val registration: String = "",
        /** Called to request a user token for the resource server. */
        val userTokenRequest: String = "",
        /** Called to validate user tokens. */
        val userTokenValidation: String = "",
    )

    data class AuthorizationServer(
        /** The resource server will try to connect to an authorization server at this URL. */
        val url: String = "",
        /** Every required endpoint of the authorization server, as relative paths. */
        val endpoints: Endpoints = Endpoints()
    )
}
