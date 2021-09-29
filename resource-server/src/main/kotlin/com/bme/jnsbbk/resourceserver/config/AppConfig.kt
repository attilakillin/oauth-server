package com.bme.jnsbbk.resourceserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

@Configuration
@ConstructorBinding
@ConfigurationProperties("application")
data class AppConfig(
    var authorizationServer: AuthorizationServer = AuthorizationServer()
) {
    data class AuthorizationServer(
        /** The resource server will try to connect to an authorization server at this URL. */
        val url: String = ""
    )
}
