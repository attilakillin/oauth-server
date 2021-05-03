package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.validators.AuthValidator
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.jwt.UserJwtHandler
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(AuthorizationController::class)
class AuthorizationControllerTests {
    @Autowired private lateinit var mockMvc: MockMvc

    @MockkBean private lateinit var authValidator: AuthValidator
    @MockkBean private lateinit var clientRepository: ClientRepository
    @MockkBean private lateinit var authCodeRepository: AuthCodeRepository
    @MockkBean private lateinit var jwtHandler: UserJwtHandler
    @MockkBean private lateinit var authCodeFactory: AuthCodeFactory

    @Test
    fun authorizationRequested_showsErrorOnValidatorSensitiveFail() {
        every { authValidator.validateSensitiveOrError(any()) } returns "Error message"

        mockMvc
            .perform(get("/authorize"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Error message")))
    }

    @Test
    fun authorizationRequested_redirectsOnValidatorAdditionalFail() {
        val uri = "an_example_uri_string"
        val message = "custom_error_message"

        every { authValidator.validateSensitiveOrError(any()) } returns null
        every { authValidator.validateAdditionalOrError(any()) } returns message

        mockMvc
            .perform(get("/authorize").param("redirect_uri", uri))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("$uri?error=$message"))
    }
}