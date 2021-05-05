package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.users.UserRepository
import com.bme.jnsbbk.oauthserver.users.entities.User
import com.bme.jnsbbk.oauthserver.utils.PasswordHasher
import com.bme.jnsbbk.oauthserver.utils.RandomString
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@ExtendWith(MockKExtension::class)
class UserJwtHandlerTests {
    private val userRepository = mockk<UserRepository>()
    private val appConfig = mockk<AppConfig>()
    private val handler = UserJwtHandler(userRepository, appConfig)

    private val user = User(RandomString.generate(), "email", PasswordHasher.hash("password"))
    private val lifespan = 60L

    @BeforeEach
    fun initializeContext() {
        val request = MockHttpServletRequest()
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
    }

    @Test
    fun createSigned_createsValidJwt() {
        every { appConfig.users.authTokenLifespan } returns lifespan
        every { userRepository.existsById(user.id) } returns true
        val token = handler.createSigned(user)
        assertTrue(handler.isUserTokenValid(token))
    }

    @Test
    fun createSigned_retainsUserId() {
        every { appConfig.users.authTokenLifespan } returns lifespan
        val token = handler.createSigned(user)
        assertEquals(user.id, handler.getUserIdFrom(token))
    }
}
