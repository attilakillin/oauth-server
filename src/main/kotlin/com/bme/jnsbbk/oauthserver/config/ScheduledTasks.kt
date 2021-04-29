package com.bme.jnsbbk.oauthserver.config

import com.bme.jnsbbk.oauthserver.authorization.AuthCodeRepository
import com.bme.jnsbbk.oauthserver.token.TokenRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import javax.transaction.Transactional

@Component
class ScheduledTasks (
    val authCodeRepository: AuthCodeRepository,
    val tokenRepository: TokenRepository
) {

    @Transactional
    @Scheduled(cron = "#{schedulingConfig.deleteExpiredEntities}")
    fun removeExpiredEntries() {
        val now = Instant.now()
        authCodeRepository.removeCodesThatExpireBefore(now)
        tokenRepository.removeTokensThatExpireBefore(now)
    }
}