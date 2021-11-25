package com.bme.jnsbbk.oauthserver.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RandomStringTests {
    @Test
    fun generate_answersWithExactLength() {
        for (i in 5..35 step 5) {
            Assertions.assertTrue(RandomString.generate(i.toLong()).length == i)
        }
    }
}
