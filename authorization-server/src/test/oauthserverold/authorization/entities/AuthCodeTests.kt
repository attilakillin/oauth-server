package com.bme.jnsbbk.oauthserverold.authorization.entities

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.authorization.entities.isExpired
import com.bme.jnsbbk.oauthserver.authorization.entities.isTimestampValid
import com.bme.jnsbbk.oauthserverold.forManyTimestampCombinations
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

class AuthCodeTests {
    private val delay: Long = 86400 // Set this to a sufficiently large value

    private fun createCodeWithTimes(iat: Instant, nbe: Instant, eat: Instant?) =
        AuthCode("", "", "", setOf(), null, iat, nbe, eat)

    @Test
    fun isExpired_isFalseForNull() {
        val now = Instant.now()
        val code = createCodeWithTimes(now, now, null)
        assertFalse(code.isExpired())
    }

    @Test
    fun isExpired_isFalseForNonExpired() {
        val now = Instant.now()
        val code = createCodeWithTimes(now, now, now.plusSeconds(delay))
        assertFalse(code.isExpired())
    }

    @Test
    fun isExpired_isTrueForExpired() {
        val now = Instant.now()
        val code = createCodeWithTimes(now, now, now.minusSeconds(delay))
        assertTrue(code.isExpired())
    }

    @Test
    fun isTimestampValid_checkAllCombinations() {
        forManyTimestampCombinations(delay) { iat, nbe, eat, expected ->
            val code = createCodeWithTimes(iat, nbe, eat)
            assertEquals(expected, code.isTimestampValid())
        }
    }
}
