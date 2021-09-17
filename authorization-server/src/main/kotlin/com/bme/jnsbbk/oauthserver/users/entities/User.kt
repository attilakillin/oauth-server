package com.bme.jnsbbk.oauthserver.users.entities

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

/**
 * Entity class representing an end user.
 *
 * These users are used as the resource owners of OAuth resources.
 *
 * Passwords must only be stored as hashed, preferably salted, encoded strings.
 */
@Entity
data class User (
    @Id val id: String,
    @Column(unique = true)
    private val username: String,
    private val password: String
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableListOf()

    override fun getUsername(): String = username
    override fun getPassword(): String = password

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}
