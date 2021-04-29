package com.bme.jnsbbk.oauthserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

// TODO Document this properly
// TODO 0-valued expirations are not implemented properly

@Configuration
@ConstructorBinding
@ConfigurationProperties("application.tokens")
data class TokenConfig (
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

@Configuration
@ConstructorBinding
@ConfigurationProperties("application.scheduling")
data class SchedulingConfig (
    /** How often should the application check for expired entities
     *  and delete them from repositories? Use 6-digit cron syntax. */
    var deleteExpiredEntities: String = "0 0 0 * * ?"
)

@Configuration
@ConstructorBinding
@ConfigurationProperties("application.debug")
data class DebugConfig (
    /** Whether default entity instances should be generated or not. */
    var defaultInstances: Boolean = false
)

@Configuration
@ConstructorBinding
@ConfigurationProperties("application.users")
data class UserConfig (
    /** How long does the user authentication token live after user login? */
    var tokenLifetime: Long = 300
)