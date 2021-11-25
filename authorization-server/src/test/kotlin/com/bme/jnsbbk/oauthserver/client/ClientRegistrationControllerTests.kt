package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.utils.getIssuerString
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(
    ClientRegistrationController::class,
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [WebSecurityConfigurer::class])],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class ClientRegistrationControllerTests {
    @Autowired private lateinit var mvc: MockMvc
    @MockkBean private lateinit var repository: ClientRepository
    @MockkBean private lateinit var service: ClientService

    init {
        mockkStatic(::getIssuerString)
    }

    private val id = "client_id"
    private val secret = "client_secret"
    private val client = Client(
        id = id,
        secret = secret,
        redirectUris = setOf(),
        tokenEndpointAuthMethod = "none",
        grantTypes = setOf(),
        responseTypes = setOf(),
        scope = setOf(),
        idIssuedAt = Instant.now(),
        secretExpiresAt = null,
        registrationAccessToken = "access_token"
    )

    @Test
    fun registerClient_withValidRequest() {
        every { service.validateClientRegistration(any()) } returns true
        every { service.createValidClient(any()) } returns client
        every { repository.save(client) } returns client
        every { getIssuerString() } returns "issuer-string"

        val request = post("/oauth/clients")
            .contentType(MediaType.APPLICATION_JSON).content("{}")

        mvc
            .perform(request)
            .andExpect(status().isOk)

        verify { repository.save(client) }
    }

    @Test
    fun registerClient_withInvalidRequest() {
        every { service.validateClientRegistration(any()) } returns false

        val request = post("/oauth/clients")
            .contentType(MediaType.APPLICATION_JSON).content("{}")

        mvc
            .perform(request)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun getClient_withValidRequest() {
        every { service.getAuthorizedClient(id, "Bearer access_token") } returns client
        every { getIssuerString() } returns "issuer-string"

        val request = get("/oauth/clients/$id")
            .header("Authorization", "Bearer access_token")

        mvc
            .perform(request)
            .andExpect(status().isOk)
    }

    @Test
    fun getClient_withValidIdButInvalidAuthorization() {
        every { service.getAuthorizedClient(id, any()) } returns null

        val request = get("/oauth/clients/$id")
            .header("Authorization", "Bearer invalid")

        mvc
            .perform(request)
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun getClient_withInvalidId() {
        every { service.getAuthorizedClient("invalid_id", any()) } returns null

        val request = get("/oauth/clients/invalid_id")
            .header("Authorization", "Bearer access_token")

        mvc
            .perform(request)
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun deleteClient_withValidRequest() {
        every { service.getAuthorizedClient(id, "Bearer access_token") } returns client
        every { repository.delete(client) } just runs

        val request = delete("/oauth/clients/$id")
            .header("Authorization", "Bearer access_token")

        mvc
            .perform(request)
            .andExpect(status().isNoContent)

        verify { repository.delete(client) }
    }

    @Test
    fun deleteClient_withValidIdButInvalidAuthorization() {
        every { service.getAuthorizedClient(id, any()) } returns null

        val request = delete("/oauth/clients/$id")
            .header("Authorization", "Bearer invalid")

        mvc
            .perform(request)
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun deleteClient_withInvalidId() {
        every { service.getAuthorizedClient("invalid_id", any()) } returns null

        val request = delete("/oauth/clients/invalid_id")
            .header("Authorization", "Bearer access_token")

        mvc
            .perform(request)
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun updateClient_withValidRequest() {
        every { service.getAuthorizedClient(id, "Bearer access_token") } returns client
        every { service.validateClientUpdate(client, any()) } returns true
        every { service.updateValidClient(client, any()) } returns client
        every { repository.save(client) } returns client
        every { getIssuerString() } returns "issuer-string"

        val request = put("/oauth/clients/$id")
            .header("Authorization", "Bearer access_token")
            .contentType(MediaType.APPLICATION_JSON).content("{}")

        mvc
            .perform(request)
            .andExpect(status().isOk)

        verify { repository.save(client) }
    }

    @Test
    fun updateClient_withInvalidUpdateRequest() {
        every { service.getAuthorizedClient(id, "Bearer access_token") } returns client
        every { service.validateClientUpdate(client, any()) } returns false

        val request = put("/oauth/clients/$id")
            .header("Authorization", "Bearer access_token")
            .contentType(MediaType.APPLICATION_JSON).content("{}")

        mvc
            .perform(request)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun updateClient_withValidIdButInvalidAuthorization() {
        every { service.getAuthorizedClient(id, any()) } returns null

        val request = put("/oauth/clients/$id")
            .header("Authorization", "Bearer invalid")
            .contentType(MediaType.APPLICATION_JSON).content("{}")

        mvc
            .perform(request)
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun updateClient_withInvalidId() {
        every { service.getAuthorizedClient("invalid_id", any()) } returns null

        val request = put("/oauth/clients/invalid_id")
            .header("Authorization", "Bearer access_token")
            .contentType(MediaType.APPLICATION_JSON).content("{}")

        mvc
            .perform(request)
            .andExpect(status().isUnauthorized)
    }
}