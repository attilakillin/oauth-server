package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(
    AuthorizationController::class,
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [WebSecurityConfigurer::class])],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class AuthorizationControllerTests {
    @Autowired private lateinit var mvc: MockMvc
    @MockkBean private lateinit var service: AuthorizationService

    @Test
    fun authorizationRequested_withInvalidClient() {
        every { service.validateRequestClient(any()) } returns false

        mvc
            .perform(get("/oauth/authorize"))
            .andExpect(view().name("generic-error"))
    }

    @Test
    fun authorizationRequested_withInvalidRedirectUri() {
        every { service.validateRequestClient(any()) } returns true
        every { service.validateRequestUri(any()) } returns false

        mvc
            .perform(get("/oauth/authorize"))
            .andExpect(view().name("generic-error"))
    }

    @Test
    fun authorizationRequested_withInvalidResponseType() {
        every { service.validateRequestClient(any()) } returns true
        every { service.validateRequestUri(any()) } returns true
        every { service.validateRequestResponseType(any()) } returns false

        mvc
            .perform(get("/oauth/authorize").param("redirect_uri", "url"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("url?error=unsupported_response_type"))
    }

    @Test
    fun authorizationRequested_withInvalidScope() {
        every { service.validateRequestClient(any()) } returns true
        every { service.validateRequestUri(any()) } returns true
        every { service.validateRequestResponseType(any()) } returns true
        every { service.validateRequestScope(any()) } returns false

        mvc
            .perform(get("/oauth/authorize").param("redirect_uri", "url"))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("url?error=invalid_scope"))
    }

    @Test
    fun authorizationRequested_withValidRequest() {
        val authRequest = AuthRequest(
            clientId = "client_id",
            redirectUri = "redirect_uri",
            responseType = "code",
            scope = setOf("scope"),
            state = null,
            nonce = null
        )

        every { service.validateRequestClient(any()) } returns true
        every { service.validateRequestUri(any()) } returns true
        every { service.validateRequestResponseType(any()) } returns true
        every { service.validateRequestScope(any()) } returns true
        every { service.convertToValidRequest(any()) } returns authRequest

        mvc
            .perform(get("/oauth/authorize").param("redirect_uri", "url"))
            .andExpect(view().name("auth-form"))
    }

    @Test
    fun approveAuthorization_withInvalidRequestId() {
        mvc
            .perform(post("/oauth/authorize").param("reqId", "invalid"))
            .andExpect(view().name("generic-error"))
    }
}
