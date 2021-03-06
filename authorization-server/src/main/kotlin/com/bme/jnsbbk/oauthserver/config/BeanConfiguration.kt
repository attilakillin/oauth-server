package com.bme.jnsbbk.oauthserver.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Configures beans that can have multiple implementations.
 *
 * As repositories are automatically instanced from the relevant interfaces,
 * they don't need to be declared here.
 */
@Configuration
class BeanConfiguration {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
