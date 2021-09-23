package com.bme.jnsbbk.oauthserver.resource

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.resource.entities.UnvalidatedRS
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/oauth/resource")
class RSRegistrationController(
    private val rsRepository: RSRepository,
    private val appConfig: AppConfig
) {

    @PostMapping
    fun handleRegistration(
        @RequestBody params: UnvalidatedRS,
        request: HttpServletRequest
    ): ResponseEntity<ResourceServer> {
        if (request.remoteHost !in appConfig.resourceServers.urls) {
            badRequest("unknown_resource_server_url: " + appConfig.resourceServers.urls + " // " + request.remoteHost)
        }

        val id = RandomString.generateUntil { !rsRepository.existsById(it) }
        val secret = RandomString.generate()
        val rs = ResourceServer(id, secret, request.remoteHost, params.scope)

        rsRepository.save(rs)
        return ResponseEntity.ok(rs)
    }
}
