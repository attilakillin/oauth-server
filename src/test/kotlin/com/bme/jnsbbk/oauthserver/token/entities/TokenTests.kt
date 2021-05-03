package com.bme.jnsbbk.oauthserver.token.entities

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

class TokenTests {
    private val delay: Long = 86400 // Set this to a sufficiently large value

    private fun createTokenWithTimes(iat: Instant, nbe: Instant, eat: Instant?) =
        Token("", TokenType.ACCESS, "", "", setOf(), iat, nbe, eat)

    @Test
    fun isExpired_isFalseForNull() {
        val now = Instant.now()
        val token = createTokenWithTimes(now, now, null)
        assertFalse(token.isExpired())
    }

    @Test
    fun isExpired_isFalseForNonExpired() {
        val now = Instant.now()
        val token = createTokenWithTimes(now, now, now.plusSeconds(delay))
        assertFalse(token.isExpired())
    }

    @Test
    fun isExpired_isTrueForExpired() {
        val now = Instant.now()
        val token = createTokenWithTimes(now, now, now.minusSeconds(delay))
        assertTrue(token.isExpired())
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

                    val token = createTokenWithTimes(iat, nbe, eat)
                    val expected = iatValid && nbeValid && eatValid
                    assertEquals(expected, token.isTimestampValid())
                }
            }
        }
    }
}
