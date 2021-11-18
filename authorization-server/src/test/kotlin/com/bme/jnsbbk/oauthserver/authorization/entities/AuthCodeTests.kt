package com.bme.jnsbbk.oauthserver.authorization.entities

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant

class AuthCodeTests {
    private val delay: Long = 60

    @Test
    fun isExpired_withNoExpiration() {
        val now = Instant.now()
        Assertions.assertFalse(code(now, now, null).isExpired())
    }

    @Test
    fun isExpired_withFutureExpiration() {
        val now = Instant.now()
        Assertions.assertFalse(code(now, now, now.plusSeconds(delay)).isExpired())
    }

    @Test
    fun isExpired_withPastExpiration() {
        val now = Instant.now()
        Assertions.assertTrue(code(now, now, now.minusSeconds(delay)).isExpired())
    }

    @Test
    fun isTimestampValid_withAllCombinations() {
        forTimestamps { iat, nbe, exp, expected ->
            Assertions.assertEquals(expected, code(iat, nbe, exp).isTimestampValid())
        }
    }

    private fun code(iat: Instant, nbe: Instant, exp: Instant?): AuthCode {
        return AuthCode(
            value = "",
            clientId = "",
            userId = "",
            scope = setOf(),
            nonce = null,
            issuedAt = iat,
            notBefore = nbe,
            expiresAt = exp
        )
    }

    private fun forTimestamps(actions: (Instant, Instant, Instant?, Boolean) -> Unit) {
        val now = Instant.now()
        val iatList = listOf(now.minusSeconds(delay), now.plusSeconds(delay))
        val nbeList = listOf(now.minusSeconds(delay), now.plusSeconds(delay))
        val expList = listOf(now.minusSeconds(delay), now.plusSeconds(delay), null)

        iatList.forEach { iat ->
            val iatValid = iat.isBefore(now)
            nbeList.forEach { nbe ->
                val nbeValid = nbe.isBefore(now)
                expList.forEach { exp ->
                    val expValid = exp?.isAfter(now) ?: true
                    actions(iat, nbe, exp, iatValid && nbeValid && expValid)
                }
            }
        }
    }
}
