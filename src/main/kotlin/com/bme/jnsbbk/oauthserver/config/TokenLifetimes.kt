package com.bme.jnsbbk.oauthserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

@Configuration
@ConstructorBinding
@ConfigurationProperties("token-lifetimes")
data class TokenLifetimes (
    var authorizationCode: LifetimeConfig = LifetimeConfig(),
    var accessToken: LifetimeConfig = LifetimeConfig(),
    var refreshToken: LifetimeConfig = LifetimeConfig()
) {
    data class LifetimeConfig (
        /** How long after issuing does the token become valid? In seconds. */
        val notBeforeOffset: Long = 0,
        /** How long does the token live? In seconds, counting from the notBefore timestamp. */
        val lifetime: Long = 0
    )
}