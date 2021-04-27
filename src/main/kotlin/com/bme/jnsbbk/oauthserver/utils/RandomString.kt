package com.bme.jnsbbk.oauthserver.utils

import java.security.SecureRandom
import kotlin.streams.asSequence

/** Provides secure random string generation methods. */
object RandomString {
    private const val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    private val random = SecureRandom()

    /** Generates a new random alphanumeric string with the given [length]. */
    fun generate(length: Long = 32) =
        random.ints(length, 0, chars.length)
            .asSequence()
            .map(chars::get)
            .joinToString("")

    /** Generates new random alphanumeric strings until the condition is met. */
    inline fun generateUntil(length: Long = 32, condition: (String) -> Boolean): String {
        var string: String
        do {
            string = generate(length)
        } while (!condition(string))
        return string
    }
}