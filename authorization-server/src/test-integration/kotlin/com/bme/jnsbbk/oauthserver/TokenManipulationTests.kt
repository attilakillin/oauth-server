@file:Suppress("FunctionName")

package com.bme.jnsbbk.oauthserver

import com.bme.jnsbbk.oauthserver.components.entities.IntegrationClientRequest
import com.bme.jnsbbk.oauthserver.components.methods.*
import com.bme.jnsbbk.oauthserver.resource.ResourceServerRepository
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
class TokenManipulationTests {
    @Autowired private lateinit var mvc: MockMvc
    @Autowired private lateinit var webClient: WebClient
    @Autowired private lateinit var resourceServerRepository: ResourceServerRepository
    @Autowired private lateinit var tokenRepository: TokenRepository
    @Autowired private lateinit var tokenService: TokenService

    @BeforeEach
    fun configureWebClient() {
        setupWebClient(webClient)
        resourceServerRepository.deleteAll()
    }

    @Test
    fun tokenManipulation_testIntrospection() {
        val scope = setOf("read", "write")
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = scope
        )
        val resource = runResourceServerRegistration(mvc, scope)

        val client = runDynamicClientRegistration(mvc, request)
        val code = authorizeClientWithAllScopes(webClient, client, "code")
        val tokens = exchangePayloadForTokens(mvc, client, "authorization_code", code)

        val introspect = introspectAccessToken(mvc, resource, tokens.accessToken)

        Assertions.assertEquals("true", introspect.active)
        Assertions.assertEquals(scope, introspect.scope)
        Assertions.assertEquals(client.id, introspect.clientId)
        Assertions.assertNotNull(introspect.sub)
        Assertions.assertEquals("test", introspect.username)
    }

    @Test
    fun tokenManipulation_testIntrospection_withClientCredentials() {
        val scope = setOf("read", "write")
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = scope,
            grantTypes = setOf("client_credentials")
        )
        val resource = runResourceServerRegistration(mvc, scope)

        val client = runDynamicClientRegistration(mvc, request)
        val tokens = exchangePayloadForTokens(mvc, client, "client_credentials",
            scope.joinToString(" "))

        val introspect = introspectAccessToken(mvc, resource, tokens.accessToken)

        Assertions.assertEquals("true", introspect.active)
        Assertions.assertEquals(scope, introspect.scope)
        Assertions.assertEquals(client.id, introspect.clientId)
        Assertions.assertNull(introspect.sub)
        Assertions.assertNull(introspect.username)
    }

    @Test
    fun tokenManipulation_testAccessTokenRevocation() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write")
        )

        val client = runDynamicClientRegistration(mvc, request)
        val code = authorizeClientWithAllScopes(webClient, client, "code")
        val tokens = exchangePayloadForTokens(mvc, client, "authorization_code", code)

        revokeToken(mvc, client, tokens.accessToken)

        Assertions.assertNull(tokenService.convertFromJwt(tokens.accessToken))
    }

    @Test
    fun tokenManipulation_testRefreshTokenRevocation() {
        val request = IntegrationClientRequest(
            redirectUris = setOf("redirect_uri"),
            scope = setOf("read", "write")
        )

        val client = runDynamicClientRegistration(mvc, request)
        val code = authorizeClientWithAllScopes(webClient, client, "code")
        val tokens = exchangePayloadForTokens(mvc, client, "authorization_code", code)

        Assertions.assertNotNull(tokens.refreshToken)
        revokeToken(mvc, client, tokens.refreshToken!!)

        Assertions.assertFalse(tokenRepository.existsById(tokens.refreshToken))
    }
}
