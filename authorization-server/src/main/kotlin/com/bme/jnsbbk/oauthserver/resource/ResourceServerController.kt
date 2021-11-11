package com.bme.jnsbbk.oauthserver.resource

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.jwt.ResourceServerJwtHandler
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServerRequest
import com.bme.jnsbbk.oauthserver.user.entities.User
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/oauth/resource")
class ResourceServerController(
    private val resourceServerService: ResourceServerService,
    private val resourceServerJwtHandler: ResourceServerJwtHandler,
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
        if (params.baseUrl == null || params.baseUrl !in appConfig.resourceServers.urls) {
            badRequest("unknown_resource_server_url: ${params.baseUrl}")
        }

        if (params.id != null && params.secret != null) {
            val saved = resourceServerService.getServerById(params.id)
            if (saved != null && saved.secret == params.secret && saved.scope == params.scope) {
                return ResponseEntity.ok(saved)
            }
        }

        if (resourceServerService.serverExistsByUrl(params.baseUrl)) {
            badRequest("resource_server_already_registered")
        }

        val rs = resourceServerService.createServer(params.baseUrl, params.scope)
        return ResponseEntity.ok(rs)
    }

    /**
     * When called with a valid resource server ID, redirect the caller to that resource server
     * with a JWT containing the ID of the currently logged-in user.
     *
     * Given a resource server ID and an optional redirect path, create a token that contains the ID
     * of the current user, and redirect to the URL (and optional path) of the resource server with
     * the token encoded as a query parameter. The URL is extracted from the resource server DB entity.
     */
    @GetMapping("/user")
    fun retrieveCurrentUser(
        @RequestParam("server_id") serverId: String,
        @RequestParam("redirect_path") redirectPath: String?,
        @AuthenticationPrincipal user: User
    ): String {
        val server = resourceServerService.getServerById(serverId) ?: unauthorized("unknown_resource_server")
        val redirectUri = server.baseUrl + (redirectPath ?: "")

        val token = resourceServerJwtHandler.createSigned(serverId, user.id)
        val encodedToken = Base64.getUrlEncoder().encodeToString(token.toByteArray(Charsets.UTF_8))

        val url = UriComponentsBuilder
            .fromUriString(redirectUri)
            .queryParam("token", encodedToken)
            .toUriString()

        return "redirect:$url"
    }

    /**
     * Given a resource server token, and the credentials belonging to that resource server, validate
     * the token, and respond with the properties of the user contained in said token.
     *
     * May respond with 401 if the server or the token is invalid, with 400 if the user doesn't exist,
     * or with 200 and a JSON body containing the properties of the user.
     */
    @PostMapping("/user/validate")
    @ResponseBody
    fun validateUserToken(
        @RequestHeader("Authorization") header: String?,
        @RequestBody token: String
    ): ResponseEntity<Map<String, String>> {
        val server = resourceServerService.authenticateBasic(header) ?: unauthorized("unknown_resource_server")

        if (!resourceServerJwtHandler.isTokenValid(token, server)) unauthorized("invalid_token")

        val user = resourceServerJwtHandler.getUserFrom(token) ?: badRequest("invalid_user")

        return ResponseEntity.ok(mapOf("username" to user.username))
    }
}
