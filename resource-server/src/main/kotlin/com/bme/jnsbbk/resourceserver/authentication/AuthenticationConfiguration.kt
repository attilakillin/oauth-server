package com.bme.jnsbbk.resourceserver.authentication

import com.bme.jnsbbk.resourceserver.configuration.AppConfig
import com.bme.jnsbbk.resourceserver.configuration.PropertyRepository
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class AuthenticationConfiguration(
    private val propertyRepository: PropertyRepository,
    private val appConfig: AppConfig
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        super.addInterceptors(registry)

        val interceptor = AuthenticationInterceptor(propertyRepository, appConfig)
        registry.addInterceptor(interceptor).addPathPatterns("/")
    }
}
