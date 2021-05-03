package com.bme.jnsbbk.oauthserver.utils

import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Provides hashing and comparison methods for string passwords. */
object PasswordHasher {

    private fun generateSalt() = RandomString.generate(16).toByteArray()

    private fun hashWithSalt(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 128)
        val hash = SecretKeyFactory
            .getInstance("PBKDF2WithHmacSHA1")
            .generateSecret(spec).encoded
        spec.clearPassword()

        val encoder = Base64.getUrlEncoder().withoutPadding()
        return encoder.encodeToString(salt) + ':' + encoder.encodeToString(hash)
    }

    /** Hashes the given [password] with a new randomly generated salt. */
    fun hash(password: String) = hashWithSalt(password, generateSalt())

    /** Checks if the given [password] has the same hash as the given [hash].
     *  The [hash] value should contain the salt used at creation. */
    fun matchesHash(password: String, hash: String): Boolean {
        val salt = Base64.getUrlDecoder().decode(hash.substringBefore(':'))
        return hashWithSalt(password, salt) == hash
    }
}
