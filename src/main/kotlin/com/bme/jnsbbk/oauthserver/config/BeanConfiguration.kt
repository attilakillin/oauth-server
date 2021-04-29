package com.bme.jnsbbk.oauthserver.config

import com.bme.jnsbbk.oauthserver.authorization.validators.AuthValidator
import com.bme.jnsbbk.oauthserver.authorization.validators.BasicAuthValidator
import com.bme.jnsbbk.oauthserver.client.validators.BasicClientValidator
import com.bme.jnsbbk.oauthserver.client.validators.ClientValidator
import com.bme.jnsbbk.oauthserver.token.validators.BasicClientAuthenticator
import com.bme.jnsbbk.oauthserver.token.validators.ClientAuthenticator
import com.bme.jnsbbk.oauthserver.users.validators.BasicUserValidator
import com.bme.jnsbbk.oauthserver.users.validators.UserValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configures beans that can have multiple implementations.
 *
 * As repositories are automatically instanced from the relevant interfaces,
 * they don't need to be declared here.
 */
@Configuration
class BeanConfiguration {
    @Bean("clientValidator")
    fun getClientValidator(): ClientValidator = BasicClientValidator()

    @Bean("authValidator")
    fun getAuthValidator(): AuthValidator = BasicAuthValidator()

    @Bean("clientAuthenticator")
    fun getClientAuthenticator(): ClientAuthenticator = BasicClientAuthenticator()

    @Bean("userValidator")
    fun getUserValidator(): UserValidator = BasicUserValidator()
}