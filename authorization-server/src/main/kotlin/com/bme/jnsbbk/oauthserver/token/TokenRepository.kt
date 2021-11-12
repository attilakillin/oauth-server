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
    /** Remove expired tokens. This method is called periodically. */
    @Modifying
    @Query("DELETE FROM Token token WHERE token.expiresAt IS NOT NULL AND token.expiresAt < :time")
    fun removeTokensThatExpireBefore(time: Instant): Int

    /** Find a token by both value and token type. */
    fun findByValueAndType(value: String, type: TokenType): Token?

    /** Find every token that the given user authorized. */
    fun findAllByUserId(userId: String): List<Token>
}
