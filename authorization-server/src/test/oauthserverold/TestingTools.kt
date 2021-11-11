package com.bme.jnsbbk.oauthserverold

import java.time.Instant

class ValidationException : Exception()
fun onError(): Nothing = throw ValidationException()

inline fun forManyTimestampCombinations(delay: Long, actions: (Instant, Instant, Instant?, Boolean) -> Unit) {
    val now = Instant.now()
    val iats = listOf(now.minusSeconds(delay), now.plusSeconds(delay))
    val nbes = listOf(now.minusSeconds(delay), now.plusSeconds(delay))
    val eats = listOf(now.minusSeconds(delay), now.plusSeconds(delay), null)

    iats.forEach { iat ->
        val iatValid = iat.isBefore(now)
        nbes.forEach { nbe ->
            val nbeValid = nbe.isBefore(now)
            eats.forEach { eat ->
                val eatValid = eat == null || eat.isAfter(now)
                val expected = iatValid && nbeValid && eatValid
                actions(iat, nbe, eat, expected)
            }
        }
    }
}
