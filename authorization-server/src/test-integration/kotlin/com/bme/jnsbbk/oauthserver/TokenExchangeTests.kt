@file:Suppress("FunctionName")

package com.bme.jnsbbk.oauthserver

import com.bme.jnsbbk.oauthserver.components.entities.IntegrationClientRequest
import com.bme.jnsbbk.oauthserver.components.methods.authorizeClientWithAllScopes
import com.bme.jnsbbk.oauthserver.components.methods.exchangePayloadForTokens
import com.bme.jnsbbk.oauthserver.components.methods.runDynamicClientRegistration
import com.bme.jnsbbk.oauthserver.components.methods.setupWebClient
import com.bme.jnsbbk.oauthserver.token.TokenRepository
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
class TokenExchangeTests {
    @Autowired private lateinit var mvc: MockMvc
    @Autowired private lateinit var webClient: WebClient
    @Autowired private lateinit var tokenRepository: TokenRepository
    @Autowired private lateinit var tokenService: TokenService

    @BeforeEach
    fun configureWebClient() {
        setupWebClient(webClient)
    }

    @Test
    fun tokenExchange_testAuthorizationCodeExchange() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write")
        )

        val client = runDynamicClientRegistration(mvc, request)
        val code = authorizeClientWithAllScopes(webClient, client, "code")

        val tokens = exchangePayloadForTokens(mvc, client, "authorization_code", code)

        Assertions.assertNotNull(tokenService.convertFromJwt(tokens.accessToken))
        Assertions.assertEquals(request.scope, tokens.scope)
        Assertions.assertNotNull(tokens.refreshToken)
        Assertions.assertTrue(tokenRepository.existsById(tokens.refreshToken!!))
        Assertions.assertNull(tokens.idToken)
    }

    @Test
    fun tokenExchange_testAuthorizationCodeExchange_withOpenIdScope() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write", "openid")
        )

        val client = runDynamicClientRegistration(mvc, request)
        val code = authorizeClientWithAllScopes(webClient, client, "code")

        val tokens = exchangePayloadForTokens(mvc, client, "authorization_code", code)

        Assertions.assertNotNull(tokens.idToken)
    }

    @Test
    fun tokenExchange_testRefreshTokenExchange() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write")
        )

        val client = runDynamicClientRegistration(mvc, request)
        val code = authorizeClientWithAllScopes(webClient, client, "code")
        val tokens = exchangePayloadForTokens(mvc, client, "authorization_code", code)

        Assertions.assertNotNull(tokens.refreshToken)
        val newTokens = exchangePayloadForTokens(mvc, client, "refresh_token", tokens.refreshToken!!)

        Assertions.assertNotNull(tokenService.convertFromJwt(newTokens.accessToken))
        Assertions.assertEquals(request.scope, newTokens.scope)
    }

    @Test
    fun tokenExchange_testClientCredentialsExchange() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write"),
            grantTypes = setOf("client_credentials")
        )

        val client = runDynamicClientRegistration(mvc, request)
        val tokens = exchangePayloadForTokens(mvc, client, "client_credentials",
            client.scope.joinToString(" "))

        Assertions.assertNotNull(tokenService.convertFromJwt(tokens.accessToken))
        Assertions.assertEquals(request.scope, tokens.scope)
        Assertions.assertNull(tokens.refreshToken)
        Assertions.assertNull(tokens.idToken)
    }
}
