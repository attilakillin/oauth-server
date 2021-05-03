package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface AuthCodeRepository : JpaRepository<AuthCode, String> {

    @Modifying
    @Query("DELETE FROM AuthCode code WHERE code.expiresAt IS NOT NULL AND code.expiresAt < :time")
    fun removeCodesThatExpireBefore(time: Instant): Int
}
