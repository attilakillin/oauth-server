@file:Suppress("JpaAttributeTypeInspection")
package com.bme.jnsbbk.oauthserver.user.entities

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.*

/**
 * Entity class representing an end user. Passwords must be stored in an encrypted format.
 *
 * These users are used as the resource owners of OAuth resources. They have a unique username,
 * a password of their choice, and a randomly generated ID used for identification.
 *
 * Each user may also have extra personal information that they have entered, these are stored in
 * a [UserInfo] object.
 *
 * This class implements the Spring Security [UserDetails] interface to ensure smooth interoperation
 * between Spring Security user authentication and usage in this application. As such, the class has
 * many, otherwise unnecessary methods.
 */
@Entity
data class User(
    /** The ID of the user, used for identification throughout the OAuth ecosystem. */
    @Id val id: String,
    /** The username of the user, must be unique. */
    @Column(unique = true) private val username: String,
    /** The password (or some form of digest of it) of the user. */
    private val password: String,
    /** The roles of the user, necessary because of the Spring integration. */
    private val roles: Set<String> = setOf("USER"),
    /** Additional information stored about the user. */
    @OneToOne(cascade = [CascadeType.ALL]) @JoinColumn(name = "info_id", referencedColumnName = "id")
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

/** Shorthand extension that creates a simple string-string map of the user's name. */
fun User.mapOfUsername(): Map<String, String> = mapOf("username" to username)
