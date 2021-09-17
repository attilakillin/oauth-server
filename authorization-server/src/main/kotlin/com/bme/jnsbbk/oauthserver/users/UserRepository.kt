package com.bme.jnsbbk.oauthserver.users

import com.bme.jnsbbk.oauthserver.users.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, String> {
    fun findByUsername(username: String): User?
}
