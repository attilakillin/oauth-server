package com.bme.jnsbbk.oauthserver.user

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(UserController::class)
class UserControllerTests {
    @Autowired private lateinit var mockMvc: MockMvc

    @MockkBean private lateinit var userRepository: UserRepository
    @MockkBean private lateinit var passwordEncoder: PasswordEncoder
    @MockkBean private lateinit var userService: UserService

    private val username = "username"
    private val password = "password"
    private val passwordConfirm = "password"

    @Test
    fun handleRegistration_rejectIfPasswordConfirmationNotValid() {
        every { userService.userExistsByUsername(any()) } returns false

        mockMvc
            .perform(
                post("/user/register")
                    .param("email", username)
                    .param("password", password)
                    .param("password_confirm", "something_else")
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun handleRegistration_rejectIfUserAlreadyExists() {
        every { userService.userExistsByUsername(any()) } returns true

        mockMvc
            .perform(
                post("/user/register")
                    .param("username", username)
                    .param("password", password)
                    .param("password_confirm", passwordConfirm)
            )
            .andExpect(status().isOk)
        // TODO fix test, it's ok, but loads the registration page again
    }
}
