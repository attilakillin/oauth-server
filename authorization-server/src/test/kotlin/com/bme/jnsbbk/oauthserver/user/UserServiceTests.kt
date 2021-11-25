package com.bme.jnsbbk.oauthserver.user

import com.bme.jnsbbk.oauthserver.user.entities.User
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockKExtension::class)
class UserServiceTests {
    private val repository = mockk<UserRepository>()
    private val passwordEncoder = mockk<PasswordEncoder>()

    private val service = UserService(repository, passwordEncoder)

    private val user = User("user_id", "username", "password_hash")

    /** Test function: loadUserByUsername() */

    @Test
    fun loadUserByUsername_withValidUsername() {
        every { repository.findByUsername(user.username) } returns user

        Assertions.assertEquals(user, service.loadUserByUsername(user.username))
    }

    @Test
    fun loadUserByUsername_withInvalidUsername() {
        every { repository.findByUsername(any()) } returns null

        Assertions.assertThrows(Exception::class.java) {
            service.loadUserByUsername("invalid_username")
        }
    }

    /** Test function: userExistsByUsername() */

    @Test
    fun userExistsByUsername_withBothVariants() {
        every { repository.findByUsername(any()) } returns user
        Assertions.assertTrue(service.userExistsByUsername(user.username))

        every { repository.findByUsername(any()) } returns null
        Assertions.assertFalse(service.userExistsByUsername(user.username))
    }

    /** Test function: userExistsById() */

    @Test
    fun userExistsById_withBothVariants() {
        every { repository.findByIdOrNull(any()) } returns user
        Assertions.assertTrue(service.userExistsById(user.username))

        every { repository.findByIdOrNull(any()) } returns null
        Assertions.assertFalse(service.userExistsById(user.username))
    }

    /** Test function: getUserById() */

    @Test
    fun getUserById_withAllVariants() {
        every { repository.findByIdOrNull(any()) } returns user
        Assertions.assertEquals(user, service.getUserById(user.id))

        every { repository.findByIdOrNull(any()) } returns null
        Assertions.assertEquals(null, service.getUserById(user.id))

        Assertions.assertEquals(null, service.getUserById(null))
    }

    /** Test function: createUser() */

    @Test
    fun createUser_withValidValues() {
        every { repository.existsById(any()) } returns false
        every { passwordEncoder.encode("password") } returns "password_hash"
        every { repository.save(any()) } answers { firstArg() }

        val user = service.createUser("username", "password")

        Assertions.assertEquals("username", user.username)
        Assertions.assertEquals("password_hash", user.password)

        verify { passwordEncoder.encode("password") }
        verify { repository.save(any()) }
    }

    /** Test function: updateUserInfo() */

    @Test
    fun updateUserInfo_withValidValues() {
        every { repository.save(any()) } answers { firstArg() }

        val info = service.updateUserInfo(user, "name", "email", "address")
        Assertions.assertEquals("name", info.name)
        Assertions.assertEquals("email", info.email)
        Assertions.assertEquals("address", info.address)

        verify { repository.save(any()) }
    }

    @Test
    fun updateUserInfo_withValidNullValues() {
        every { repository.save(any()) } answers { firstArg() }

        val info = service.updateUserInfo(user, null, null, null)
        Assertions.assertEquals("", info.name)
        Assertions.assertEquals("", info.email)
        Assertions.assertEquals("", info.address)

        verify { repository.save(any()) }
    }
}
