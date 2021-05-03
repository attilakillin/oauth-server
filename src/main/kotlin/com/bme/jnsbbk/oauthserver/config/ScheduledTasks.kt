package com.bme.jnsbbk.oauthserver.config

import com.bme.jnsbbk.oauthserver.authorization.AuthCodeRepository
import com.bme.jnsbbk.oauthserver.token.TokenRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import javax.transaction.Transactional

@Component
class ScheduledTasks(
    val authCodeRepository: AuthCodeRepository,
    val tokenRepository: TokenRepository
) {
    /**
     * Regularly removes expired entities from multiple repositories.
     *
     * Both authorization codes and OAuth tokens usually have expiration times.
     * To prevent the buildup of expired, otherwise unused entries in their
     * database tables, expired tokens are removed each time this function is called.
     *
     * @see AppConfig
     */
    @Transactional
    @Scheduled(cron = "#{appConfig.scheduling.deleteExpiredEntities}")
    fun removeExpiredEntries() {
        val now = Instant.now()
        authCodeRepository.removeCodesThatExpireBefore(now)
        tokenRepository.removeTokensThatExpireBefore(now)
    }
}
