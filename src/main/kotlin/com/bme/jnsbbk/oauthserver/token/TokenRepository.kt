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
    @Query("DELETE FROM Token token WHERE token.expiresAt IS NOT NULL AND token.expiresAt < :time")
    fun removeTokensThatExpireBefore(time: Instant): Int

    fun findFirstByValueAndType(value: String, type: TokenType): Token?

    @JvmDefault // With this annotation, JPA won't try to map the function to a query.
    fun findAccessById(value: String): Token? = findFirstByValueAndType(value, TokenType.ACCESS)
    @JvmDefault
    fun findRefreshById(value: String): Token? = findFirstByValueAndType(value, TokenType.REFRESH)
}