@file:Suppress("FunctionName")
package com.bme.jnsbbk.oauthserver

import com.bme.jnsbbk.oauthserver.authorization.AuthCodeRepository
import com.bme.jnsbbk.oauthserver.components.entities.IntegrationClientRequest
import com.bme.jnsbbk.oauthserver.components.methods.authorizeClientWithAllScopes
import com.bme.jnsbbk.oauthserver.components.methods.runDynamicClientRegistration
import com.bme.jnsbbk.oauthserver.components.methods.setupWebClient
import com.bme.jnsbbk.oauthserver.token.TokenService
import com.gargoylesoftware.htmlunit.WebClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@ActiveProfiles("integration")
@Sql(scripts = ["classpath:data-integration.sql"])
@WithUserDetails("test")
@AutoConfigureMockMvc
class FrontChannelAuthorizationTests {
    @Autowired private lateinit var mvc: MockMvc
    @Autowired private lateinit var authCodeRepository: AuthCodeRepository
    @Autowired private lateinit var tokenService: TokenService
    @Autowired private lateinit var webClient: WebClient

    @BeforeEach
    fun configureWebClient() {
        setupWebClient(webClient)
    }

    @Test
    fun frontChannelAuthorization_testAuthCodeAuthorization() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write")
        )

        val client = runDynamicClientRegistration(mvc, request)
        val code = authorizeClientWithAllScopes(webClient, client, "code")

        Assertions.assertTrue(authCodeRepository.existsById(code))
    }

    @Test
    fun frontChannelAuthorization_testImplicitAuthorization() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write"),
            grantTypes = setOf("implicit")
        )

        val client = runDynamicClientRegistration(mvc, request)
        val jwt = authorizeClientWithAllScopes(webClient, client, "token")

        val token = tokenService.convertFromJwt(jwt)

        Assertions.assertNotNull(token)
    }
}
