package com.bme.jnsbbk.oauthserver.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class SyntacticSugarTests {
    private fun encode(string: String): String {
        return Base64.getUrlEncoder().encode(string.toByteArray()).toString(Charsets.UTF_8)
    }

    @Test
    fun decodeAsHttpBasic_withValidValues() {
        val input = "Basic " + encode("id:secret")
        val result = input.decodeAsHttpBasic()

        Assertions.assertNotNull(result)
        Assertions.assertEquals("id", result?.first)
        Assertions.assertEquals("secret", result?.second)
    }

    @Test
    fun decodeAsHttpBasic_withNoPrefix() {
        Assertions.assertNull(encode("id:secret").decodeAsHttpBasic())
    }

    @Test
    fun decodeAsHttpBasic_withNoColon() {
        val input = "Basic " + encode("no_colon")
        Assertions.assertNull(input.decodeAsHttpBasic())
    }

    @Test
    fun decodeAsHttpBasic_withInvalidBase64() {
        Assertions.assertNull("Basic invalidBase64".decodeAsHttpBasic())
    }
}
