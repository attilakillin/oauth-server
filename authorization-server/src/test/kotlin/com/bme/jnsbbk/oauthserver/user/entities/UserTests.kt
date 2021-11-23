package com.bme.jnsbbk.oauthserver.user.entities

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UserTests {

    @Test
    fun mapOfUsername_withValidUser() {
        val user = User("id", "custom_username", "password")
        val map = user.mapOfUsername()

        Assertions.assertEquals(user.username, map["username"])
    }
}
