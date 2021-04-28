package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.token.entities.Token
import com.bme.jnsbbk.oauthserver.token.entities.TokenType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface TokenRepository : JpaRepository<Token, String> {

    @Modifying
    @Query("DELETE FROM Token token WHERE token.expiresAt < :time")
    fun removeExpiredTokens(time: Instant): Int

    fun findFirstByValueAndType(value: String, type: TokenType): Token?

    /* "Default functions" that don't get mapped by JPA */
    @JvmDefault
    fun findAccessById(value: String): Token? = findFirstByValueAndType(value, TokenType.ACCESS)
    @JvmDefault
    fun findRefreshById(value: String): Token? = findFirstByValueAndType(value, TokenType.REFRESH)
}