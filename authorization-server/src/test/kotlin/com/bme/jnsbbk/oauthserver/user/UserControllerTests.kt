package com.bme.jnsbbk.oauthserver.user

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

@WebMvcTest(UserController::class)
class UserControllerTests {
    @Autowired private lateinit var mockMvc: MockMvc

    @MockkBean private lateinit var userRepository: UserRepository

    private val username = "username"
    private val password = "password"

    @Test
    fun handleRegistration_rejectIfRegistrationNotValid() {
        mockMvc
            .perform(
                post("/user/register")
                    .param("email", username)
                    .param("password", password)
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun handleRegistration_rejectIfEmailAlreadyExists() {
        every { userRepository.existsById(any()) } returns true

        mockMvc
            .perform(
                post("/user/register")
                    .param("email", username)
                    .param("password", password)
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun handleRegistration_acceptRegistration() {
        val response = "this should be returned"
        every { userRepository.existsById(any()) } returns false
        every { userRepository.save(any()) } answers { firstArg() }

        mockMvc
            .perform(
                post("/user/register")
                    .param("email", username)
                    .param("password", password)
            )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(response)))
    }
}
