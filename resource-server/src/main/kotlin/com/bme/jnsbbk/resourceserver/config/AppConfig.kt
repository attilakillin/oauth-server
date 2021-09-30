package com.bme.jnsbbk.resourceserver.config

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
    data class AuthorizationServer(
        /** The resource server will try to connect to an authorization server at this URL. */
        val url: String = "",
        /** This endpoint is used for registering the resource server at the authorization server. */
        val registrationEndpoint: String = "",
        /** This endpoint is used for getting the currently authenticated user. */
        val authenticationEndpoint: String = "",
        /** When no user is authenticated, the resource server will redirect callers here. */
        val userLoginEndpoint: String = ""
    )
}
