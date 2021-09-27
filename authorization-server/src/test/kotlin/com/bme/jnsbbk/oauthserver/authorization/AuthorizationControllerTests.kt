package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.user.UserRepository
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthorizationController::class)
class AuthorizationControllerTests {
    @Autowired private lateinit var mockMvc: MockMvc

    @MockkBean private lateinit var authRequestService: AuthRequestService
    @MockkBean private lateinit var clientRepository: ClientRepository
    @MockkBean private lateinit var userRepository: UserRepository
    @MockkBean private lateinit var passwordEncoder: PasswordEncoder
    @MockkBean private lateinit var authCodeRepository: AuthCodeRepository
    @MockkBean private lateinit var authCodeFactory: AuthCodeFactory

    @Test
    fun authorizationRequested_showsErrorOnValidatorSensitiveFail() {
        every { authRequestService.isSensitiveInfoValid(any()) } returns Pair(false, "Error message")

        mockMvc
            .perform(get("/oauth/authorize"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Error message")))
    }

    @Test
    fun authorizationRequested_redirectsOnValidatorAdditionalFail() {
        val uri = "an_example_uri_string"
        val message = "custom_error_message"

        every { authRequestService.isSensitiveInfoValid(any()) } returns Pair(true, "")
        every { authRequestService.isAdditionalInfoValid(any()) } returns Pair(false, message)

        mockMvc
            .perform(get("/oauth/authorize").param("redirect_uri", uri))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("$uri?error=$message"))
    }

    @Test
    fun authorizationRequested_fillsTemplateCorrectlyOnSuccess() {
        val name = "test_random_name"

        val client = Client(RandomString.generate()).apply { extraData["client_name"] = name }
        val request = AuthRequest(
            clientId = client.id,
            redirectUri = "",
            responseType = "",
            scope = setOf("random", "xyz"),
            state = null
        )

        every { authRequestService.isSensitiveInfoValid(any()) } returns Pair(true, "")
        every { authRequestService.isAdditionalInfoValid(any()) } returns Pair(true, "")
        every { authRequestService.convertToValidRequest(any()) } returns request
        every { clientRepository.findById(client.id) } returns Optional.of(client)

        mockMvc
            .perform(get("/oauth/authorize"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("random")))
            .andExpect(content().string(containsString("xyz")))
    }

    @Test
    fun approveAuthorization_showsErrorOnInvalidRequest() {
        // TODO Test fails because no principal is present
        mockMvc
            .perform(post("/oauth/authorize").param("reqId", "invalid value"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("error")))
    }
}
