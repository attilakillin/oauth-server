package com.bme.jnsbbk.oauthserver.exceptions

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping

private const val urlPrefix = "/test/apiException"
private const val thrownMessage = "custom_message"

@WebMvcTest(ApiExceptionHandlerTestController::class)
class ApiExceptionHandlerTests {
    @Autowired private lateinit var mockMvc: MockMvc

    @Test
    fun exceptionHandling_basicApiException() {
        mockMvc
            .perform(get("$urlPrefix/teapot"))
            .andExpect(status().isIAmATeapot)
    }

    @Test
    fun exceptionHandling_badRequestWithMessage() {
        mockMvc
            .perform(get("$urlPrefix/400"))
            .andExpect(status().isBadRequest)
            .andExpect(content().string(containsString("error")))
            .andExpect(content().string(containsString(thrownMessage)))
    }

    @Test
    fun exceptionHandling_emptyUnauthorized() {
        mockMvc
            .perform(get("$urlPrefix/401"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().string(not(containsString("error"))))
    }
}

@Controller
class ApiExceptionHandlerTestController {

    @GetMapping("$urlPrefix/teapot")
    fun throwBasicApiException(): Nothing = throw ApiException(HttpStatus.I_AM_A_TEAPOT)

    @GetMapping("$urlPrefix/400")
    fun throwBadRequestExceptionWithMessage(): Nothing = badRequest(thrownMessage)

    @GetMapping("$urlPrefix/401")
    fun throwEmptyUnauthorizedException(): Nothing = unauthorized()
}
