package com.bme.jnsbbk.oauthserver.jwt

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RSAKeyRepository : JpaRepository<RSAKey, String>