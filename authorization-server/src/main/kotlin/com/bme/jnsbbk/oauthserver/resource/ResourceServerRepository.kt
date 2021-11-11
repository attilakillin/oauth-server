package com.bme.jnsbbk.oauthserver.resource

import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ResourceServerRepository : JpaRepository<ResourceServer, String> {
    fun findByBaseUrl(url: String): ResourceServer?
}
