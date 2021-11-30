package com.bme.jnsbbk.oauthserver.user

import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.user.entities.UserInfo
import com.bme.jnsbbk.oauthserver.user.entities.fromNullable
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.bme.jnsbbk.oauthserver.utils.getIssuerString
import dev.samstevens.totp.code.HashingAlgorithm
import dev.samstevens.totp.qr.QrData
import dev.samstevens.totp.qr.ZxingPngQrGenerator
import dev.samstevens.totp.util.Utils
import org.springframework.data.repository.findByIdOrNull
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
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException(username)

        if (user.isMfaUsed) {
            val mfaRoles = user.roles.filter { it != "USER" }.toMutableSet()
            mfaRoles.add("PRE_MFA_AUTH")
            return user.copy(roles = mfaRoles)
        } else {
            return user
        }
    }

    /** Returns true or false depending on whether a user with the given username exists or not. */
    fun userExistsByUsername(username: String): Boolean {
        return userRepository.findByUsername(username) != null
    }

    /** Returns true or false depending on whether a user with the given ID exists or not. */
    fun userExistsById(id: String): Boolean {
        return userRepository.findByIdOrNull(id) != null
    }

    /** Returns a user by its ID, or null, if no such user exists. */
    fun getUserById(id: String?): User? {
        return if (id != null) userRepository.findByIdOrNull(id) else null
    }

    /** Creates and persists a user with the given credentials and roles. */
    fun createUser(
        username: String,
        password: String,
        useMfa: Boolean = false,
        roles: Set<String> = setOf("USER")
    ): User {
        val id = RandomString.generateUntil { !userRepository.existsById(it) }
        val user = User(
            id = id,
            username = username,
            password = passwordEncoder.encode(password),
            isMfaUsed = useMfa,
            roles = roles
        )
        return userRepository.save(user)
    }

    /** Update the user's info with the specified values. Returns the updated user info. */
    fun updateUserInfo(user: User, name: String?, email: String?, address: String?): UserInfo {
        user.info = UserInfo.fromNullable(name, email, address)
        userRepository.save(user)
        return user.info
    }

    /** Generate an image data string containing a QR code for MFA registration. */
    fun getMfaQRUrl(user: User): String {
        val qrData = QrData.Builder()
            .label(user.username)
            .secret(user.mfaSecret)
            .issuer("oauth-server :: " + getIssuerString())
            .algorithm(HashingAlgorithm.SHA256)
            .digits(6)
            .period(30)
            .build()

        val generator = ZxingPngQrGenerator()
        return Utils.getDataUriForImage(generator.generate(qrData), generator.imageMimeType)
    }
}
