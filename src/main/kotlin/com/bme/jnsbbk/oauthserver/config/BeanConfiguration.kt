package com.bme.jnsbbk.oauthserver.config

import com.bme.jnsbbk.oauthserver.authorization.validators.AuthValidator
import com.bme.jnsbbk.oauthserver.authorization.validators.BasicAuthValidator
import com.bme.jnsbbk.oauthserver.client.validators.BasicClientValidator
import com.bme.jnsbbk.oauthserver.client.validators.ClientValidator
import com.bme.jnsbbk.oauthserver.token.validators.BasicTokenValidator
import com.bme.jnsbbk.oauthserver.token.validators.TokenValidator
import com.bme.jnsbbk.oauthserver.users.validators.BasicUserValidator
import com.bme.jnsbbk.oauthserver.users.validators.UserValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfiguration {
    @Bean("clientValidator")
    fun getClientValidator(): ClientValidator = BasicClientValidator()

    @Bean("authValidator")
    fun getAuthValidator(): AuthValidator = BasicAuthValidator()

    @Bean("tokenValidator")
    fun getTokenValidator(): TokenValidator = BasicTokenValidator()

    @Bean("userValidator")
    fun getUserValidator(): UserValidator = BasicUserValidator()
}