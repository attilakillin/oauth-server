package com.bme.jnsbbk.oauthserver.user.entities

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class UserInfo(
    val name: String = "",
    val email: String = "",
    val address: String = ""
) {
    @Id @GeneratedValue
    private val id: Long = 0

    companion object
}

fun UserInfo.Companion.fromNullable(name: String?, email: String?, address: String?): UserInfo =
    UserInfo(name ?: "", email ?: "", address ?: "")
