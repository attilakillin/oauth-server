package com.bme.jnsbbk.oauthserver.token

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TokenRepository : JpaRepository<Token, String> {
    fun findFirstByValueAndType(value: String, type: TokenType): Token?

    /* "Default functions" that don't get mapped by JPA */
    @JvmDefault
    fun findAccessById(value: String): Token? = findFirstByValueAndType(value, TokenType.ACCESS)
    @JvmDefault
    fun findRefreshById(value: String): Token? = findFirstByValueAndType(value, TokenType.REFRESH)
}