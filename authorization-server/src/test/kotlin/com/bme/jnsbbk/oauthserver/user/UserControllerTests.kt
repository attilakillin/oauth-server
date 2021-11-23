package com.bme.jnsbbk.oauthserver.user

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(UserController::class)
class UserControllerTests {
    @Autowired private lateinit var mvc: MockMvc
    @MockkBean private lateinit var userService: UserService
    @MockkBean private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun handleRegistration_withValidValues() {
        every { userService.userExistsByUsername("username") } returns false
        every { userService.createUser("username", "password") } returns mockk()
        every { passwordEncoder.encode(any()) } returns "hash"

        mvc
            .perform(post("/user/register")
                .param("username", "username")
                .param("password", "password")
                .param("password_confirm", "password"))
            .andExpect(status().is3xxRedirection)

        verify { userService.createUser("username", "password") }
    }

    @Test
    fun handleRegistration_withExistingUsername() {
        every { userService.userExistsByUsername("username") } returns true

        mvc
            .perform(post("/user/register")
                .param("username", "username")
                .param("password", "password")
                .param("password_confirm", "password"))
            .andExpect(view().name("user-register"))

        verify(exactly = 0) { userService.createUser(any(), any()) }
    }

    @Test
    fun handleRegistration_withDifferentPasswordAndPasswordConfirm() {
        every { userService.userExistsByUsername("username") } returns false

        mvc
            .perform(post("/user/register")
                .param("username", "username")
                .param("password", "password")
                .param("password_confirm", "something_else"))
            .andExpect(view().name("user-register"))

        verify(exactly = 0) { userService.createUser(any(), any()) }
    }
}
