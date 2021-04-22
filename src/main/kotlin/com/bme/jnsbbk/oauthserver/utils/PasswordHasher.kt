package com.bme.jnsbbk.oauthserver.utils

import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private fun salt() = RandomString.generate(16).toByteArray()

    private fun hashWithSalt(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 128)
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val hash = factory.generateSecret(spec).encoded
        spec.clearPassword()

        return encoder.encodeToString(salt) + ':' + encoder.encodeToString(hash)
    }

    fun hash(password: String): String {
        return hashWithSalt(password, salt())
    }

    fun matchesHash(password: String, hash: String): Boolean {
        val decoder = Base64.getUrlDecoder()
        val salt = decoder.decode(hash.substringBefore(':'))

        val newHash = hashWithSalt(password, salt)

        return hash == newHash
    }
}