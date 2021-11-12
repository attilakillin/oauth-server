package com.bme.jnsbbk.oauthserver.user

import com.bme.jnsbbk.oauthserver.user.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {
    /** Finds a user by their username. Returns null if no such user exists. */
    fun findByUsername(username: String): User?
}
