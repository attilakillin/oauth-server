package com.bme.jnsbbk.oauthserver.exceptions

import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(
    ExceptionTestingController::class,
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [WebSecurityConfigurer::class])],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class ApiExceptionHandlerTests {
    @Autowired private lateinit var mvc: MockMvc

    @Test
    fun testBadRequestException() {
        val message = "message"

        mvc
            .perform(get("/throw/400/$message"))
            .andExpect(status().isBadRequest)
            .andExpect(content().string(containsString(message)))
    }

    @Test
    fun testUnauthorizedException() {
        val message = "message"

        mvc
            .perform(get("/throw/401/$message"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().string(containsString(message)))
    }
}
