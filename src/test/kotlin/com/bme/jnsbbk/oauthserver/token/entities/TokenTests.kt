package com.bme.jnsbbk.oauthserver.token.entities

import org.junit.jupiter.api.Test
import java.time.Instant

class TokenTests {
    private val delay: Long = 86400

    private fun createTokenWithTimes(issuedAt: Instant, notBefore: Instant, expiresAt: Instant?) =
        Token("", TokenType.ACCESS, "", "", setOf(), issuedAt, notBefore, expiresAt)

    @Test
    fun isExpired_isFalseForNull() {
        val now = Instant.now()
        val token = createTokenWithTimes(now, now, null)
        assert(!token.isExpired())
    }

    @Test
    fun isExpired_isFalseForNonExpired() {
        val now = Instant.now()
        val token = createTokenWithTimes(now, now, now.plusSeconds(delay))
        assert(!token.isExpired())
    }

    @Test
    fun isExpired_isTrueForExpired() {
        val now = Instant.now()
        val token = createTokenWithTimes(now, now, now.minusSeconds(delay))
        assert(token.isExpired())
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

                    val expected = iatValid && nbeValid && eatValid
                    val token = createTokenWithTimes(iat, nbe, eat)
                    assert(token.isTimestampValid() == expected)
                }
            }
        }
    }
}