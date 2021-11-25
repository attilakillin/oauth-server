package com.bme.jnsbbk.oauthserver.home

import com.bme.jnsbbk.oauthserver.token.TokenRepository
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.user.entities.UserInfo
import com.bme.jnsbbk.oauthserver.user.entities.fromNullable
import com.ninjasquad.springmockk.MockkBean
import io.mockk.*
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant

@WebMvcTest(
    HomeController::class,
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [WebSecurityConfigurer::class])],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class HomeControllerTests {
    @Autowired private lateinit var mvc: MockMvc
    @MockkBean private lateinit var userService: UserService
    @MockkBean private lateinit var tokenRepository: TokenRepository

    private val token = Token(
        value = "value",
        type = TokenType.ACCESS,
        clientId = "client_id",
        userId = "user_id",
        scope = setOf("scope"),
        issuedAt = Instant.now(),
        notBefore = Instant.now(),
        expiresAt = null
    )

    @Test
    fun redirectToHome_worksAsExpected() {
        mvc
            .perform(get("/"))
            .andExpect(status().is3xxRedirection)
    }

    @Test
    fun getHomeRoot_worksAsExpected() {
        mvc
            .perform(get("/home"))
            .andExpect(status().isOk)
            .andExpect(view().name("home-root"))
    }

    @Test
    fun getUserInfo_worksAsExpected() {
        mvc
            .perform(get("/home/userinfo"))
            .andExpect(status().isOk)
            .andExpect(view().name("home-userinfo"))
    }

    @Test
    fun getAuthorizations_worksAsExpected() {
        every { tokenRepository.findAllByUserId("user_id") } returns emptyList()

        mvc
            .perform(get("/home/authorizations"))
            .andExpect(status().isOk)
            .andExpect(view().name("home-authorizations"))
    }

    @Test
    fun postUserInfo_withProperValues() {
        val name = "Name"
        val email = "test@email.com"
        val address = "Test Address Street"

        every { userService.updateUserInfo(any(), name, email, address) } returns UserInfo(name, email, address)

        mvc
            .perform(post("/home/userinfo")
                .param("name", name)
                .param("email", email)
                .param("address", address))
            .andExpect(view().name("home-userinfo"))
            .andExpect(content().string(containsString(name)))
            .andExpect(content().string(containsString(email)))
            .andExpect(content().string(containsString(address)))

        verify { userService.updateUserInfo(any(), name, email, address) }
    }

    @Test
    fun postUserInfo_withNullValues() {
        every {
            userService.updateUserInfo(any(), null, null, null)
        } returns UserInfo.fromNullable(null, null, null)

        mvc
            .perform(post("/home/userinfo"))
            .andExpect(view().name("home-userinfo"))

        verify { userService.updateUserInfo(any(), null, null, null) }
    }

    @Test
    fun revokeToken_withValidTokenToRevoke() {
        every { tokenRepository.findByIdOrNull(token.value) } returns token
        every { tokenRepository.deleteById(token.value) } just runs
        every { tokenRepository.findAllByUserId("user_id") } returns listOf(token)

        mvc
            .perform(post("/home/authorizations/revoke")
                .param("token", token.value))
            .andExpect(view().name("home-authorizations"))

        verify { tokenRepository.deleteById(token.value) }
    }

    @Test
    fun revokeToken_withExistingButInvalidToken() {
        val token = token.copy(userId = "other_id")
        every { tokenRepository.findByIdOrNull(token.value) } returns token
        every { tokenRepository.findAllByUserId("user_id") } returns emptyList()

        mvc
            .perform(post("/home/authorizations/revoke")
                .param("token", token.value))
            .andExpect(view().name("home-authorizations"))

        verify(exactly = 0) { tokenRepository.deleteById(any()) }
    }

    @Test
    fun revokeToken_withNonexistentToken() {
        every { tokenRepository.findByIdOrNull("nonexistent_id") } returns null
        every { tokenRepository.findAllByUserId("user_id") } returns emptyList()

        mvc
            .perform(post("/home/authorizations/revoke")
                .param("token", "nonexistent_id"))
            .andExpect(view().name("home-authorizations"))

        verify(exactly = 0) { tokenRepository.deleteById(any()) }
    }
}
