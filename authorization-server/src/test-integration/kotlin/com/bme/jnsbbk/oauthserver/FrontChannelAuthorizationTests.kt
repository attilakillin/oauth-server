package com.bme.jnsbbk.oauthserver

import com.bme.jnsbbk.oauthserver.authorization.AuthCodeRepository
import com.bme.jnsbbk.oauthserver.components.entities.IntegrationClientRequest
import com.bme.jnsbbk.oauthserver.components.methods.authorizeClientWithAllScopes
import com.bme.jnsbbk.oauthserver.components.methods.runDynamicClientRegistration
import com.gargoylesoftware.htmlunit.ScriptException
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener
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
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL

@SpringBootTest
@ActiveProfiles("integration")
@Sql(scripts = ["classpath:data-integration.sql"])
@WithUserDetails("test")
@AutoConfigureMockMvc
class FrontChannelAuthorizationTests {
    @Autowired private lateinit var mvc: MockMvc
    @Autowired private lateinit var authCodeRepository: AuthCodeRepository
    @Autowired private lateinit var webClient: WebClient

    @BeforeEach
    fun configureWebClient() {
        webClient.options.isThrowExceptionOnScriptError = false
        webClient.options.isThrowExceptionOnFailingStatusCode = false
        webClient.javaScriptErrorListener = object : JavaScriptErrorListener {
            override fun scriptException(page: HtmlPage, ex: ScriptException) {}
            override fun timeoutError(page: HtmlPage, allowedTime: Long, executionTime: Long) {}
            override fun malformedScriptURL(page: HtmlPage, url: String, ex: MalformedURLException) {}
            override fun loadScriptError(page: HtmlPage, scriptUrl: URL, ex: Exception) {}
            override fun warn(message: String, sourceName: String, line: Int, lineSource: String, lineOffset: Int) {}
        }
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
}
