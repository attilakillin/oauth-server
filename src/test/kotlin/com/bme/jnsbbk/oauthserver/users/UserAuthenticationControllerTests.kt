package com.bme.jnsbbk.oauthserver.users

import com.bme.jnsbbk.oauthserver.jwt.UserJwtHandler
import com.bme.jnsbbk.oauthserver.users.entities.User
import com.bme.jnsbbk.oauthserver.utils.PasswordHasher
import com.bme.jnsbbk.oauthserver.utils.RandomString
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

@WebMvcTest(UserAuthenticationController::class)
class UserAuthenticationControllerTests {
    @Autowired private lateinit var mockMvc: MockMvc

    @MockkBean private lateinit var userRepository: UserRepository
    @MockkBean private lateinit var jwtHandler: UserJwtHandler

    private val email = "email"
    private val password = "password"

    @Test
    fun handleLogin_rejectIfNoMatchingEmail() {
        every { userRepository.findByEmail(any()) } returns null

        mockMvc
            .perform(
                post("/user/login")
                    .param("email", email)
                    .param("password", password)
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun handleLogin_rejectIfNoMatchingPassword() {
        val user = User(RandomString.generate(), email, "invalidHash")
        every { userRepository.findByEmail(any()) } returns user

        mockMvc
            .perform(
                post("/user/login")
                    .param("email", email)
                    .param("password", password)
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun handleLogin_acceptIfValid() {
        val user = User(RandomString.generate(), email, PasswordHasher.hash(password))
        val response = "something that can be checked"
        every { userRepository.findByEmail(any()) } returns user
        every { jwtHandler.createSigned(any()) } returns response

        mockMvc
            .perform(
                post("/user/login")
                    .param("email", email)
                    .param("password", password)
            )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(response)))
    }

    @Test
    fun handleAuthValidation_rejectsIfInvalid() {
        every { jwtHandler.isUserTokenValid(any()) } returns false

        mockMvc
            .perform(post("/user/validate").param("userToken", "invalid"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun handleAuthValidation_acceptsIfValid() {
        every { jwtHandler.isUserTokenValid(any()) } returns true

        mockMvc
            .perform(post("/user/validate").param("userToken", "invalid"))
            .andExpect(status().isOk)
    }
}
