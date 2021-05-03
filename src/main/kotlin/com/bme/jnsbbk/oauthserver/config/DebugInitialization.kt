package com.bme.jnsbbk.oauthserver.config

import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder

/**
 * A post-initialization class that creates default instances of entity classes to aid debugging.
 *
 * Only executes once, and only if the default-instances property is enabled in the application
 * configuration.
 *
 * @see AppConfig
 */
@Component
class DebugInitialization(private val appConfig: AppConfig) {
    private val serverUrl = "http://localhost:8080"
    private var executed = false
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Automatically called after the environment is initialized.
     *
     * Creates a default client and a default user instance that can be used during testing.
     * Prints relevant information for both entities, as well as a well-formed authorization
     * link to the standard output.
     */
    @EventListener(ContextRefreshedEvent::class)
    fun onRefreshEvent() {
        if (appConfig.debug.createDefaultInstances && !executed) {
            executed = true
            logger.info("Default entity instancing enabled, creating default entities...")
            createDefaultClient()
            createDefaultUser()
        }
    }

    private fun createDefaultClient() {
        val template = RestTemplate()
        val url = "$serverUrl/register"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val entity = HttpEntity<String>(
            """
                {
                    "client_name": "Test client",
                    "redirect_uris": ["http://localhost:8081/callback"],
                    "client_uri": "http://localhost:8081",
                    "grant_types": ["authorization_code"],
                    "scope": "alfa beta gamma delta"
                }
            """.trimIndent(),
            headers
        )

        val response = template.postForEntity<String>(url, entity)
        if (response.statusCode != HttpStatus.OK) {
            logger.warn("Error creating default client instance: POST returned with status ${response.statusCodeValue}!")
            return
        }
        val result = jacksonObjectMapper().readTree(response.body)
        logger.info("Created default client instance, details below:")
        println(result.toPrettyString())

        logger.info("Creating sample authorization request link:")

        val builder = UriComponentsBuilder.fromUriString("$serverUrl/authorize")
            .queryParam("client_id", result["client_id"].toString().trim('"'))
            .queryParam("scope", result["scope"].toString().trim('"').replace(' ', '+'))
            .queryParam("redirect_uri", result["redirect_uris"][0].toString().trim('"'))
            .queryParam("response_type", "code")
            .queryParam("state", RandomString.generate())
            .build()

        println("\n  " + builder.toUriString() + "\n")
    }

    private fun createDefaultUser() {
        val email = "admin@admin.hu"
        val password = "12345678"

        val template = RestTemplate()
        val url = "$serverUrl/user/register"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val params = LinkedMultiValueMap<String, String>()
        params.add("email", email)
        params.add("password", password)
        val entity = HttpEntity<MultiValueMap<String, String>>(params, headers)
        val response = template.postForEntity<String>(url, entity)

        if (response.statusCode != HttpStatus.OK) {
            logger.warn("Error creating default user instance: POST returned with status ${response.statusCodeValue}!")
            return
        }

        logger.info("Created default user instance, details below:")
        println("\n  Email: $email, password: $password\n")
    }
}
