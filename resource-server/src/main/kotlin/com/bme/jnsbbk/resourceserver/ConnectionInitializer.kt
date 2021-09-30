package com.bme.jnsbbk.resourceserver

import com.bme.jnsbbk.resourceserver.config.AppConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Component
class ConnectionInitializer(
    val appConfig: AppConfig
) : SmartInitializingSingleton {

    @Retryable(maxAttempts = 5, backoff = Backoff(delay = 3000, multiplier = 1.5))
    override fun afterSingletonsInstantiated() {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val request = HttpEntity("{ \"scope\": \"alpha beta gamma delta\" }", headers)

        val url = appConfig.authorizationServer.url + appConfig.authorizationServer.registrationEndpoint
        val response = RestTemplate().postForEntity<String>(url, request)

        val logger = LoggerFactory.getLogger(this::class.java)
        logger.info(response.body)
    }
}
