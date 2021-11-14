package com.bme.jnsbbk.oauthserverold.token.entities

import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import com.bme.jnsbbk.oauthserver.token.entities.isExpired
import com.bme.jnsbbk.oauthserver.token.entities.isTimestampValid
import com.bme.jnsbbk.oauthserverold.forManyTimestampCombinations
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
        forManyTimestampCombinations(delay) { iat, nbe, eat, expected ->
            val token = createTokenWithTimes(iat, nbe, eat)
            assertEquals(expected, token.isTimestampValid())
        }
    }
}
