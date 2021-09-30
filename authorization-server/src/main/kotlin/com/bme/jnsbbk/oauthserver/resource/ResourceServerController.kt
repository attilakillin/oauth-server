package com.bme.jnsbbk.oauthserver.resource

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServerRequest
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.security.Principal
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/oauth/resource")
class ResourceServerController(
    private val resourceServerService: ResourceServerService,
    private val appConfig: AppConfig
) {

    /**
     * Called any time a resource server tries to register.
     *
     * The sender must have its URL registered in the application's configuration file, and
     * must provide a JSON body that corresponds to the [params] parameter.
     *
     * This method returns a JSON object containing the credentials of the newly registered
     * resource server.
     */
    @PostMapping
    fun handleRegistration(
        @RequestBody params: ResourceServerRequest,
        request: HttpServletRequest
    ): ResponseEntity<ResourceServer> {
        val url = request.remoteHost

        var accepted = false
        for (allowedUrl in appConfig.resourceServers.urls) {
            val matcher = IpAddressMatcher(allowedUrl)
            if (matcher.matches(url)) {
                accepted = true
                break
            }
        }

        if (!accepted) {
            badRequest("unknown_resource_server_url: $url")
        }
        if (resourceServerService.resourceServerExists(url)) {
            badRequest("resource_server_already_registered")
        }

        val rs = resourceServerService.createResourceServer(url, params.scope)
        return ResponseEntity.ok(rs)
    }

    @GetMapping("/user")
    fun retrieveCurrentUser(
        @RequestHeader("Authorization") header: String?,
        authentication: Authentication?
    ): ResponseEntity<Map<String, String>> {
        if (resourceServerService.authenticateBasic(header) == null) {
            unauthorized("resource_server_unauthorized")
        }

        if (authentication == null || !authentication.isAuthenticated) {
            badRequest("user_unauthenticated")
        }

        val username = (authentication.principal as Principal).name
        return ResponseEntity.ok(mapOf("username" to username))
    }
}
