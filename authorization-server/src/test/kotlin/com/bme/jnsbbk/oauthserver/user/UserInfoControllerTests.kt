package com.bme.jnsbbk.oauthserver.user

import com.bme.jnsbbk.oauthserver.jwt.AccessTokenHandler
import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.user.entities.UserInfo
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(
    UserInfoController::class,
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [WebSecurityConfigurer::class])],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class UserInfoControllerTests {
    @Autowired private lateinit var mvc: MockMvc
    @MockkBean private lateinit var accessTokenHandler: AccessTokenHandler
    @MockkBean private lateinit var userService: UserService

    private val user = User(
        id = "user_id",
        username = "username",
        password = "password"
    ).apply { info = UserInfo("Name", "Email", "Address") }
    private val token = Token(
        value = "value",
        type = TokenType.ACCESS,
        clientId = "client_id",
        userId = user.id,
        scope = setOf("openid"),
        issuedAt = Instant.now(),
        notBefore = Instant.now(),
        expiresAt = null
    )

    @Test
    fun handleRequest_withValidValues() {
        every { accessTokenHandler.convertToValidToken("jwt_string") } returns token
        every { userService.getUserById(user.id) } returns user

        mvc
            .perform(get("/oauth/openid/userinfo")
                .header("Authorization", "Bearer jwt_string"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(user.id)))
    }

    @Test
    fun handleRequest_withValidValues_andAllOpenIdScopes() {
        val token = token.copy(scope = setOf("openid", "profile", "email", "address"))

        every { accessTokenHandler.convertToValidToken("jwt_string") } returns token
        every { userService.getUserById(user.id) } returns user

        mvc
            .perform(get("/oauth/openid/userinfo")
                .header("Authorization", "Bearer jwt_string"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(user.id)))
            .andExpect(content().string(containsString(user.info.name)))
            .andExpect(content().string(containsString(user.info.email)))
            .andExpect(content().string(containsString(user.info.address)))
    }

    @Test
    fun handleRequest_withInvalidJwt() {
        every { accessTokenHandler.convertToValidToken("jwt_string") } returns null

        mvc
            .perform(get("/oauth/openid/userinfo")
                .header("Authorization", "Bearer jwt_string"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun handleRequest_withValidJwtButInvalidTokenValues() {
        val token = token.copy(userId = null, expiresAt = Instant.now().minusSeconds(60))

        every { accessTokenHandler.convertToValidToken("jwt_string") } returns token

        mvc
            .perform(get("/oauth/openid/userinfo")
                .header("Authorization", "Bearer jwt_string"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun handleRequest_withNoOpenIdScope() {
        val token = token.copy(scope = setOf("invalid_scope"))
        every { accessTokenHandler.convertToValidToken("jwt_string") } returns token

        mvc
            .perform(get("/oauth/openid/userinfo")
                .header("Authorization", "Bearer jwt_string"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun handleRequest_withNonexistentUser() {
        every { accessTokenHandler.convertToValidToken("jwt_string") } returns token
        every { userService.getUserById(user.id) } returns null

        mvc
            .perform(get("/oauth/openid/userinfo")
                .header("Authorization", "Bearer jwt_string"))
            .andExpect(status().isBadRequest)
    }
}
