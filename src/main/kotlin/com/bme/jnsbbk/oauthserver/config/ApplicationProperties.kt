package com.bme.jnsbbk.oauthserver.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

@Configuration
@ConstructorBinding
@ConfigurationProperties("application")
data class AppConfig (
    var tokens: Tokens = Tokens(),
    var scheduling: Scheduling = Scheduling(),
    var users: Users = Users(),
    var debug: Debug = Debug()
) {
    data class Tokens (
        var authCode: Lifespan = Lifespan(),
        var accessToken: Lifespan = Lifespan(),
        var refreshToken: Lifespan = Lifespan()
    ) {
        data class Lifespan (
            /** The offset of notBefore compared to issuedAt, in seconds. */
            val notBeforeOffset: Long = 0,
            /** The lifespan of a token, in seconds. 0 means it never expires. */
            val lifespan: Long = 0
        )
    }

    data class Scheduling (
        /** 6-digit cron string: how often should expired entities be deleted from databases? */
        val deleteExpiredEntities: String = "0 0 0 * * ?"
    )

    data class Users (
        /** The lifespan of a user authentication token. 0 means it never expires. */
        val authTokenLifespan: Long = 0
    )

    data class Debug (
        /** Whether to create default entity instances or not. */
        val createDefaultInstances: Boolean = false
    )
}