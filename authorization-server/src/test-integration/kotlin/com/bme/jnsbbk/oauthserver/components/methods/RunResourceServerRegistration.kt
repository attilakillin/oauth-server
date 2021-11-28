package com.bme.jnsbbk.oauthserver.components.methods

import com.bme.jnsbbk.oauthserver.components.entities.IntegrationResourceServer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

fun runResourceServerRegistration(mvc: MockMvc, scope: Set<String> = setOf("read", "write")): IntegrationResourceServer {
    val mapper = jacksonObjectMapper().findAndRegisterModules()

    val responseBody = mvc
        .post("/oauth/resource") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(mapOf(
                "base_url" to "http://resourceserver",
                "scope" to scope.joinToString(" ")
            ))
        }
        .andExpect {
            status { isOk() }
        }
        .andReturn().response.contentAsString

    return mapper.readValue(responseBody)
}
