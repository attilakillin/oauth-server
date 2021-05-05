package com.bme.jnsbbk.oauthserver.users

import com.bme.jnsbbk.oauthserver.jwt.UserJwtHandler
import com.bme.jnsbbk.oauthserver.users.validators.UserValidator
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserRegistrationController::class)
class UserRegistrationControllerTests {
    @Autowired private lateinit var mockMvc: MockMvc

    @MockkBean private lateinit var userValidator: UserValidator
    @MockkBean private lateinit var userRepository: UserRepository
    @MockkBean private lateinit var jwtHandler: UserJwtHandler

    private val email = "email"
    private val password = "password"

    @Test
    fun handleRegistration_rejectIfRegistrationNotValid() {
        every { userValidator.isRegistrationValid(any(), any()) } returns false

        mockMvc
            .perform(
                post("/user/register")
                    .param("email", email)
                    .param("password", password)
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun handleRegistration_rejectIfEmailAlreadyExists() {
        every { userValidator.isRegistrationValid(any(), any()) } returns true
        every { userRepository.existsById(any()) } returns true

        mockMvc
            .perform(
                post("/user/register")
                    .param("email", email)
                    .param("password", password)
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun handleRegistration_acceptRegistration() {
        val response = "this should be returned"
        every { userValidator.isRegistrationValid(any(), any()) } returns true
        every { userRepository.existsById(any()) } returns false
        every { userRepository.save(any()) } answers { firstArg() }
        every { jwtHandler.createSigned(any()) } returns response

        mockMvc
            .perform(
                post("/user/register")
                    .param("email", email)
                    .param("password", password)
            )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(response)))
    }
}
