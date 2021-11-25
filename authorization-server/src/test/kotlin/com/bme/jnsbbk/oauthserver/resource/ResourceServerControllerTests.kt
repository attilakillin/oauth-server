package com.bme.jnsbbk.oauthserver.resource

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.*
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(
    ResourceServerController::class,
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [WebSecurityConfigurer::class])],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class ResourceServerControllerTests {
    @Autowired private lateinit var mvc: MockMvc
    @MockkBean private lateinit var service: ResourceServerService
    @MockkBean private lateinit var appConfig: AppConfig

    private val url = "base_url"
    private val scope = setOf("scope")
    private val server = ResourceServer(
        id = "id",
        secret = "secret",
        baseUrl = url,
        scope = scope
    )

    @Test
    fun handleRegistration_withValidRequest() {
        every { appConfig.resourceServers.urls } returns listOf(url)
        every { service.serverExistsByUrl(url) } returns false
        every { service.createServer(url, scope) } returns server

        mvc
            .perform(post("/oauth/resource").contentType(MediaType.APPLICATION_JSON)
                .content(""" {"base_url": "base_url", "scope": "scope"} """))
            .andExpect(status().isOk)

        verify { service.serverExistsByUrl(url) }
        verify { service.createServer(url, scope) }
    }

    @Test
    fun handleRegistration_withNullBaseUrl() {
        mvc
            .perform(post("/oauth/resource").contentType(MediaType.APPLICATION_JSON)
                .content(""" {"scope": "scope"} """))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun handleRegistration_withInvalidBaseUrl() {
        every { appConfig.resourceServers.urls } returns listOf(url)

        mvc
            .perform(post("/oauth/resource").contentType(MediaType.APPLICATION_JSON)
                .content(""" {"base_url": "invalid_url", "scope": "scope"} """))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun handleRegistration_withExistingBaseUrl() {
        every { appConfig.resourceServers.urls } returns listOf(url)
        every { service.serverExistsByUrl(url) } returns true

        mvc
            .perform(post("/oauth/resource").contentType(MediaType.APPLICATION_JSON)
                .content(""" {"base_url": "base_url", "scope": "scope"} """))

        verify { service.serverExistsByUrl(url) }
    }

    @Test
    fun handleRegistration_withIdAndSecretThatMatches() {
        every { appConfig.resourceServers.urls } returns listOf(url)
        every { service.getServerById("id") } returns server

        mvc
            .perform(post("/oauth/resource").contentType(MediaType.APPLICATION_JSON)
                .content(""" {"id": "id", "secret": "secret", "base_url": "base_url", "scope": "scope"} """))
            .andExpect(status().isOk)

        verify { service.getServerById("id") }
    }

    @Test
    fun handleRegistration_withIdThatDoesNotExist() {
        every { appConfig.resourceServers.urls } returns listOf(url)
        every { service.getServerById("invalid_id") } returns null

        mvc
            .perform(post("/oauth/resource").contentType(MediaType.APPLICATION_JSON)
                .content(""" {"id": "invalid_id", "secret": "secret", "base_url": "base_url", "scope": "scope"} """))
            .andExpect(status().isBadRequest)

        verify { service.getServerById("invalid_id") }
    }

    @Test
    fun handleRegistration_withValidIdButInvalidSecret() {
        every { appConfig.resourceServers.urls } returns listOf(url)
        every { service.getServerById("id") } returns server

        mvc
            .perform(post("/oauth/resource").contentType(MediaType.APPLICATION_JSON)
                .content(""" {"id": "id", "secret": "invalid_secret", "base_url": "base_url", "scope": "scope"} """))
            .andExpect(status().isBadRequest)

        verify { service.getServerById("id") }
    }

    @Test
    fun handleRegistration_withValidIdAndSecretButInvalidScope() {
        every { appConfig.resourceServers.urls } returns listOf(url)
        every { service.getServerById("id") } returns server

        mvc
            .perform(post("/oauth/resource").contentType(MediaType.APPLICATION_JSON)
                .content(""" {"id": "id", "secret": "secret", "base_url": "base_url", "scope": "invalid_scope"} """))
            .andExpect(status().isBadRequest)

        verify { service.getServerById("id") }
    }

    @Test
    fun retrieveCurrentUser_withValidValues() {
        every { service.getServerById("id") } returns server
        every { service.createEncodedUserToken(server, any()) } returns "jwtEncodedToken"

        mvc
            .perform(get("/oauth/resource/user")
                .param("server_id", "id"))
            .andExpect(status().is3xxRedirection)

        verify { service.getServerById("id") }
    }

    @Test
    fun retrieveCurrentUser_withInvalidServerId() {
        every { service.getServerById("invalid_id") } returns null

        mvc
            .perform(get("/oauth/resource/user")
                .param("server_id", "invalid_id"))
            .andExpect(view().name("generic-error"))

        verify { service.getServerById("invalid_id") }
    }

    @Test
    fun validateUserToken_withValidValues() {
        val user = User("id", "username", "password")

        every { service.authenticateBasic(any()) } returns server
        every { service.isUserTokenValid(server, any()) } returns true
        every { service.getUserFromUserToken(server, any()) } returns user

        mvc
            .perform(post("/oauth/resource/user/validate")
                .content(""" { "token": "token_value" } """))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(user.username)))

        verify { service.isUserTokenValid(server, any()) }
    }

    @Test
    fun validateUserToken_withInvalidAuthentication() {
        every { service.authenticateBasic(any()) } returns null

        mvc
            .perform(post("/oauth/resource/user/validate")
                .content(""" { "token": "token_value" } """))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun validateUserToken_withInvalidToken() {
        every { service.authenticateBasic(any()) } returns server
        every { service.isUserTokenValid(server, any()) } returns false

        mvc
            .perform(post("/oauth/resource/user/validate")
                .content(""" { "token": "invalid_token" } """))
            .andExpect(status().isUnauthorized)

        verify { service.isUserTokenValid(server, any()) }
    }

    @Test
    fun validateUserToken_withNoToken() {
        every { service.authenticateBasic(any()) } returns server

        mvc
            .perform(post("/oauth/resource/user/validate"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun validateUserToken_withNonexistentUser() {
        every { service.authenticateBasic(any()) } returns server
        every { service.isUserTokenValid(server, any()) } returns true
        every { service.getUserFromUserToken(server, any()) } returns null

        mvc
            .perform(post("/oauth/resource/user/validate")
                .content(""" { "token": "token_value" } """))
            .andExpect(status().isBadRequest)

        verify { service.isUserTokenValid(server, any()) }
    }
}
