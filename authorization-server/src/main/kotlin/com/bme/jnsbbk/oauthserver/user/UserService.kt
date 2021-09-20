package com.bme.jnsbbk.oauthserver.user

import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException(username)
    }

    fun userExists(username: String): Boolean = userRepository.findByUsername(username) != null

    fun createUser(username: String, password: String): UserDetails {
        val id = RandomString.generateUntil { !userRepository.existsById(it) }
        val user = User(id, username, passwordEncoder.encode(password))
        return userRepository.save(user)
    }
}
