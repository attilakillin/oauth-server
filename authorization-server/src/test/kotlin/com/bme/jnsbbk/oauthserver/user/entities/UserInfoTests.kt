package com.bme.jnsbbk.oauthserver.user.entities

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UserInfoTests {

    @Test
    fun fromNullable_withValidValues() {
        val info = UserInfo.fromNullable("name", "email", "address")

        Assertions.assertEquals("name", info.name)
        Assertions.assertEquals("email", info.email)
        Assertions.assertEquals("address", info.address)
    }

    @Test
    fun fromNullable_withNullValues() {
        val info = UserInfo.fromNullable(null, null, null)

        Assertions.assertEquals("", info.name)
        Assertions.assertEquals("", info.email)
        Assertions.assertEquals("", info.address)
    }
}
