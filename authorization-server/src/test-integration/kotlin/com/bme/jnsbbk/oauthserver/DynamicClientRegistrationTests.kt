package com.bme.jnsbbk.oauthserver

import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.components.entities.IntegrationClient
import com.bme.jnsbbk.oauthserver.components.entities.IntegrationClientRequest
import com.bme.jnsbbk.oauthserver.components.methods.runDynamicClientRegistration
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureMockMvc
class DynamicClientRegistrationTests {
    @Autowired private lateinit var mvc: MockMvc
    @Autowired private lateinit var clientRepository: ClientRepository

    private val mapper = jacksonObjectMapper().findAndRegisterModules()

    @Test
    fun dynamicClientRegistration_testAuthCodeRegistration() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write")
        )

        val client = runDynamicClientRegistration(mvc, request)

        Assertions.assertEquals(request.redirectUris, client.redirectUris)
        Assertions.assertEquals(request.scope, client.scope)
        Assertions.assertEquals(setOf("authorization_code"), client.grantTypes)
        Assertions.assertTrue(clientRepository.existsById(client.id))
    }

    @Test
    fun dynamicClientRegistration_testImplicitRegistration() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write"),
            grantTypes = setOf("implicit")
        )

        val client = runDynamicClientRegistration(mvc, request)

        Assertions.assertEquals(request.redirectUris, client.redirectUris)
        Assertions.assertEquals(request.scope, client.scope)
        Assertions.assertEquals(request.grantTypes, client.grantTypes)
        Assertions.assertTrue(clientRepository.existsById(client.id))
    }

    @Test
    fun dynamicClientRegistration_testGetMethod() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write")
        )

        val original = runDynamicClientRegistration(mvc, request)

        val response = mvc
            .perform(get(getUri(original))
                .header("Authorization", "Bearer ${original.registrationAccessToken}"))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val client = mapper.readValue<IntegrationClient>(response)

        Assertions.assertEquals(original, client)
    }

    @Test
    fun dynamicClientRegistration_testPutMethod() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write"),
        )

        val original = runDynamicClientRegistration(mvc, request)
        val update = request.copy(
            extraData = mapOf("client_id" to original.id, "client_secret" to original.secret!!)
        )

        val response = mvc
            .perform(put(getUri(original))
                .header("Authorization", "Bearer ${original.registrationAccessToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(update)))
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val client = mapper.readValue<IntegrationClient>(response)

        Assertions.assertEquals(original, client)
    }

    @Test
    fun dynamicClientRegistration_testDeleteMethod() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write")
        )

        val client = runDynamicClientRegistration(mvc, request)
        mvc
            .perform(delete(getUri(client))
                .header("Authorization", "Bearer ${client.registrationAccessToken}"))
            .andExpect(status().isNoContent)

        Assertions.assertFalse(clientRepository.existsById(client.id))
    }

    private fun getUri(client: IntegrationClient): String {
        val uri = client.extraData["registration_client_uri"]?.replaceBefore('/', "")
        Assertions.assertNotNull(uri)
        return uri!!
    }
}
