package com.bme.jnsbbk.resourceserver

import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

@Component
class ConnectionInitializer(
    val appConfig: AppConfig,
    val propertyRepository: PropertyRepository
) : SmartInitializingSingleton {

    /**
     * Called when the application context is loaded. Connects to the authorization server.
     *
     * This method is retryable, which means that if the authorization server is not yet available,
     * it will retry the request with exponentially larger backoff times.
     */
    @Retryable(maxAttempts = 5, backoff = Backoff(delay = 3000, multiplier = 1.5))
    override fun afterSingletonsInstantiated() {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val request = HttpEntity(mapOf("scope" to "email avatar address phone_number"), headers) // TODO Don't hardcode this

        val url = appConfig.authorizationServer.url + appConfig.authorizationServer.endpoints.registration
        val response = RestTemplate().postForObject<ResponseObject>(url, request)

        propertyRepository.saveAll(listOf(
            Property(Key.ID, response.id),
            Property(Key.SECRET, response.secret),
            Property(Key.SCOPE, response.scope)
        ))
    }

    /** Private response object class that enforces type safety. */
    private data class ResponseObject(
        val id: String,
        val secret: String,
        val scope: String
    )
}
