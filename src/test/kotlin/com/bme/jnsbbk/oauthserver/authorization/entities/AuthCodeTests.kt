package com.bme.jnsbbk.oauthserver.authorization.entities

import org.junit.jupiter.api.Test
import java.time.Instant

class AuthCodeTests {
    private val delay: Long = 86400

    private fun createCodeWithTimes(issuedAt: Instant, notBefore: Instant, expiresAt: Instant?) =
        AuthCode("", "", "", setOf(), issuedAt, notBefore, expiresAt)

    @Test
    fun isExpired_isFalseForNull() {
        val now = Instant.now()
        val code = createCodeWithTimes(now, now, null)
        assert(!code.isExpired())
    }

    @Test
    fun isExpired_isFalseForNonExpired() {
        val now = Instant.now()
        val code = createCodeWithTimes(now, now, now.plusSeconds(delay))
        assert(!code.isExpired())
    }

    @Test
    fun isExpired_isTrueForExpired() {
        val now = Instant.now()
        val code = createCodeWithTimes(now, now, now.minusSeconds(delay))
        assert(code.isExpired())
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
                    val code = createCodeWithTimes(iat, nbe, eat)
                    assert(code.isTimestampValid() == expected)
                }
            }
        }
    }
}