package com.bme.jnsbbk.oauthserver.user.entities

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UserTests {

    private val user = User("id", "custom_username", "password")

    @Test
    fun mapOfUsername_withValidUser() {
        val map = user.mapOfUsername()

        Assertions.assertEquals(user.username, map["username"])
    }

    @Test
    fun userDetailsAttributes_workAsExpected() {
        Assertions.assertEquals("ROLE_USER", user.authorities.first().authority)
        Assertions.assertTrue(user.isAccountNonLocked)
        Assertions.assertTrue(user.isAccountNonExpired)
        Assertions.assertTrue(user.isCredentialsNonExpired)
        Assertions.assertTrue(user.isEnabled)
    }
}
