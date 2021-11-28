package com.bme.jnsbbk.oauthserver.components.methods

import com.bme.jnsbbk.oauthserver.components.entities.IntegrationClient
import com.bme.jnsbbk.oauthserver.components.entities.IntegrationTokenResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

fun exchangePayloadForTokens(
    mvc: MockMvc,
    client: IntegrationClient,
    grantType: String,
    payload: String
): IntegrationTokenResponse {
    val responseBody = mvc
        .post("/oauth/token") {
            headers { setBasicAuth(client.id, client.secret!!) }
            param("grant_type", grantType)
            when (grantType) {
                "authorization_code" -> param("code", payload)
                "refresh_token" -> param("refresh_token", payload)
                "client_credentials" -> param("scope", payload)
            }
        }
        .andExpect {
            status { isOk() }
        }
        .andReturn().response.contentAsString

    return jacksonObjectMapper().findAndRegisterModules().readValue(responseBody)
}
