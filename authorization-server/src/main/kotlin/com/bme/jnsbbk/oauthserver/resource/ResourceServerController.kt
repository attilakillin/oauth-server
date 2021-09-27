package com.bme.jnsbbk.oauthserver.resource

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServerRequest
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/oauth/resource")
class ResourceServerController(
    private val resourceServerRepository: ResourceServerRepository,
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
        if (request.remoteHost !in appConfig.resourceServers.urls) {
            badRequest("unknown_resource_server_url: " + request.remoteHost)
        }
        if (resourceServerRepository.findByUrl(request.remoteHost) != null) {
            badRequest("resource_server_already_registered")
        }

        val id = RandomString.generateUntil { !resourceServerRepository.existsById(it) }
        val secret = RandomString.generate()
        val rs = ResourceServer(id, secret, request.remoteHost, params.scope)

        resourceServerRepository.save(rs)
        return ResponseEntity.ok(rs)
    }
}
