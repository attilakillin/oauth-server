package com.bme.jnsbbk.oauthserver.config

import com.bme.jnsbbk.oauthserver.authorization.validators.AuthValidator
import com.bme.jnsbbk.oauthserver.authorization.validators.BasicAuthValidator
import com.bme.jnsbbk.oauthserver.client.validators.BasicClientValidator
import com.bme.jnsbbk.oauthserver.client.validators.ClientValidator
import com.bme.jnsbbk.oauthserver.repositories.TransientRepository
import com.bme.jnsbbk.oauthserver.token.validators.BasicTokenRequestValidator
import com.bme.jnsbbk.oauthserver.token.validators.TokenRequestValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringConfig {
    @Bean("clientValidator")
    fun getClientValidator(): ClientValidator = BasicClientValidator()

    @Bean("authValidator")
    fun getAuthValidator(): AuthValidator = BasicAuthValidator()

    @Bean("tokenRequestValidator")
    fun getTokenRequestValidator(): TokenRequestValidator = BasicTokenRequestValidator()
}