package com.bme.jnsbbk.oauthserver.utils

import java.security.SecureRandom
import kotlin.streams.asSequence

object RandomString {
    private const val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    private val random = SecureRandom()

    fun generate(length: Long = 32): String {
        return random.ints(length, 0, chars.length)
            .asSequence()
            .map(chars::get)
            .joinToString("")
    }
}