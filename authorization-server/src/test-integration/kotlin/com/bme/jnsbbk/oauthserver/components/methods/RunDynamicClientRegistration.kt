package com.bme.jnsbbk.oauthserver.components.methods

import com.bme.jnsbbk.oauthserver.components.entities.IntegrationClient
import com.bme.jnsbbk.oauthserver.components.entities.IntegrationClientRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

fun runDynamicClientRegistration(
    mvc: MockMvc,
    request: IntegrationClientRequest,
    expectations: ResultActions.() -> ResultActions = { andExpect(status().isOk) }
): IntegrationClient {
    val mapper = jacksonObjectMapper().findAndRegisterModules()

    val responseBody = mvc
        .perform(post("/oauth/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)))
        .expectations()
        .andReturn().response.contentAsString

    return mapper.readValue(responseBody)
}
