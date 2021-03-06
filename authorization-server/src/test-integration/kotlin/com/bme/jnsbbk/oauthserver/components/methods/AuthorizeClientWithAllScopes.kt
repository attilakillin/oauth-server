package com.bme.jnsbbk.oauthserver.components.methods

import com.bme.jnsbbk.oauthserver.components.entities.IntegrationClient
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.bme.jnsbbk.oauthserver.utils.URL
import com.gargoylesoftware.htmlunit.Page
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlInput
import com.gargoylesoftware.htmlunit.html.HtmlPage
import org.junit.jupiter.api.Assertions

fun authorizeClientWithAllScopes(
    webClient: WebClient,
    client: IntegrationClient,
    responseType: String
): String {
    val state = RandomString.generate()

    val url = URL("http://localhost/oauth/authorize").withParams(mapOf(
        "client_id" to client.id,
        "scope" to client.scope.joinToString(" "),
        "redirect_uri" to client.redirectUris.first(),
        "response_type" to responseType,
        "state" to state
    )).build()

    val page = webClient.getPage<HtmlPage>(url)
    val submit = page.forms[0].getOneHtmlElementByAttribute<HtmlInput>(
        "input", "name", "approve")

    val result = submit.click<Page>().url

    if (responseType == "code") {
        Assertions.assertNotNull(result.query)
        Assertions.assertTrue(result.query.contains("state=$state"))

        val code = result.query
                .split('&')
                .find { it.startsWith("code=") }
                ?.removePrefix("code=")

        Assertions.assertNotNull(code)
        return code!!
    }

    if (responseType == "token") {
        Assertions.assertNotNull(result.toURI().fragment)
        Assertions.assertTrue(result.toURI().fragment.contains("state=$state"))

        val token = result.toURI().fragment
                .split('&')
                .find { it.startsWith("access_token=") }
                ?.removePrefix("access_token=")

        Assertions.assertNotNull(token)
        return token!!
    }

    throw IllegalStateException("Invalid response type used!")
}
