package com.bme.jnsbbk.oauthserver.components.methods

import com.bme.jnsbbk.oauthserver.components.entities.IntegrationIntrospectResponse
import com.bme.jnsbbk.oauthserver.components.entities.IntegrationResourceServer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

fun introspectAccessToken(
    mvc: MockMvc,
    server: IntegrationResourceServer,
    accessToken: String
): IntegrationIntrospectResponse {
    val mapper = jacksonObjectMapper().findAndRegisterModules()

    val responseBody = mvc
        .post("/oauth/token/introspect") {
            headers { setBasicAuth(server.id, server.secret) }
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(mapOf("token" to accessToken))
        }
        .andExpect {
            status { isOk() }
        }
        .andReturn().response.contentAsString

    return mapper.readValue(responseBody)
}
