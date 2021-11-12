@file:Suppress("JpaAttributeTypeInspection")
package com.bme.jnsbbk.oauthserver.user.entities

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.*

/**
 * Entity class representing an end user. Passwords must be encoded as a security precaution!
 *
 * These users are used as the resource owners of OAuth resources.
 *
 * This class implements the Spring Security UserDetails interface to ensure smooth interoperation
 * between Spring Security user authentication and usage in this application.
 */
@Entity
data class User(
    @Id
    val id: String,
    @Column(unique = true)
    private val username: String,
    private val password: String,
    private val roles: Set<String> = setOf("USER"),

    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "info_id", referencedColumnName = "id")
    var info: UserInfo = UserInfo()
) : UserDetails {
    override fun getUsername(): String = username
    override fun getPassword(): String = password

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return roles.mapTo(mutableListOf()) { SimpleGrantedAuthority(it) }
    }

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}

fun User.mapOfUsername(): Map<String, String> = mapOf("username" to username)
