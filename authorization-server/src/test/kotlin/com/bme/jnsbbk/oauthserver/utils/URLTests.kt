package com.bme.jnsbbk.oauthserver.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class URLTests {

    @Test
    fun build_withNoParams_andNoFragments() {
        val url = URL("base").build()
        Assertions.assertEquals("base", url)
    }

    @Test
    fun build_withNullParam() {
        val url = URL("base").withParam("key", null).build()
        Assertions.assertEquals("base", url)
    }

    @Test
    fun build_withSingleParam() {
        val url = URL("base").withParam("key", "value").build()
        Assertions.assertEquals("base?key=value", url)
    }

    @Test
    fun build_withManyParams() {
        val url = URL("base").withParams(mapOf("key1" to "value1", "key2" to "value2")).build()
        Assertions.assertEquals("base?key1=value1&key2=value2", url)
    }

    @Test
    fun build_withNullFragment() {
        val url = URL("base").withFragment("key", null).build()
        Assertions.assertEquals("base", url)
    }

    @Test
    fun build_withSingleFragment() {
        val url = URL("base").withFragment("key", "value").build()
        Assertions.assertEquals("base#key=value", url)
    }

    @Test
    fun build_withManyFragments() {
        val url = URL("base").withFragments(mapOf("key1" to "value1", "key2" to "value2")).build()
        Assertions.assertEquals("base#key1=value1&key2=value2", url)
    }

    @Test
    fun redirect_withValidValues() {
        val url = URL("base").redirect()
        Assertions.assertEquals("redirect:base", url)
    }
}
