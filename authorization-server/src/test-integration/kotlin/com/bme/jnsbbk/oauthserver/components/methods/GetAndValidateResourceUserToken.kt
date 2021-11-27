package com.bme.jnsbbk.oauthserver.components.methods

import com.bme.jnsbbk.oauthserver.components.entities.IntegrationResourceServer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.net.URI
import java.util.*

fun getAndValidateResourceUserToken(
    mvc: MockMvc,
    server: IntegrationResourceServer
): String {
    val url = mvc
        .get("/oauth/resource/user") {
            param("server_id", server.id)
        }
        .andExpect {
            status { is3xxRedirection() }
        }
        .andReturn().response.redirectedUrl

    Assertions.assertNotNull(url)
    val token = URI(url!!).query
        .split('&')
        .find { it.startsWith("token=") }
        ?.removePrefix("token=")

    val responseBody = mvc
        .post("/oauth/resource/user/validate") {
            headers { setBasicAuth(server.id, server.secret) }
            content = Base64.getUrlDecoder().decode(token).toString(Charsets.UTF_8)
        }
        .andExpect {
            status { isOk() }
        }
        .andReturn().response.contentAsString

    return jacksonObjectMapper().readValue<Map<String, String>>(responseBody)["username"]!!
}
