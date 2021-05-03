package com.bme.jnsbbk.oauthserver.authorization.entities

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

class AuthCodeTests {
    private val delay: Long = 86400 // Set this to a sufficiently large value

    private fun createCodeWithTimes(iat: Instant, nbe: Instant, eat: Instant?) =
        AuthCode("", "", "", setOf(), iat, nbe, eat)

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
        val now = Instant.now()
        val issuedAts = listOf(now.minusSeconds(delay), now.plusSeconds(delay))
        val notBefores = listOf(now.minusSeconds(delay), now.plusSeconds(delay))
        val expiredAts = listOf(now.minusSeconds(delay), now.plusSeconds(delay), null)

        issuedAts.forEach { iat ->
            val iatValid = iat.isBefore(now)
            notBefores.forEach { nbe ->
                val nbeValid = nbe.isBefore(now)
                expiredAts.forEach { eat ->
                    val eatValid = eat == null || eat.isAfter(now)

                    val code = createCodeWithTimes(iat, nbe, eat)
                    val expected = iatValid && nbeValid && eatValid
                    assertEquals(expected, code.isTimestampValid())
                }
            }
        }
    }
}
