package com.bme.jnsbbk.oauthserver.config

import com.bme.jnsbbk.oauthserver.client.validators.BasicClientValidator
import com.bme.jnsbbk.oauthserver.client.validators.ClientValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringConfig {
    @Bean("clientValidator")
    fun getClientValidator(): ClientValidator = BasicClientValidator()
}