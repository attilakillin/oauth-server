package com.bme.jnsbbk.oauthserver.components.methods

import com.bme.jnsbbk.oauthserver.components.entities.IntegrationClient
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

fun revokeToken(
    mvc: MockMvc,
    client: IntegrationClient,
    accessToken: String
) {
    val mapper = jacksonObjectMapper().findAndRegisterModules()

    mvc
        .post("/oauth/token/revoke") {
            headers { setBasicAuth(client.id, client.secret!!) }
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(mapOf("token" to accessToken))
        }
        .andExpect {
            status { isOk() }
        }
}
