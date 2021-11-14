package com.bme.jnsbbk.oauthserver.resource

import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServer
import com.bme.jnsbbk.oauthserver.resource.entities.ResourceServerRequest
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.user.entities.mapOfUsername
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder

@Controller
@RequestMapping("/oauth/resource")
class ResourceServerController(
    private val resourceServerService: ResourceServerService,
    appConfig: AppConfig
) {
    private val config = appConfig.resourceServers

    /**
     * Called when a potential resource server tries to register.
     *
     * The request must include a base URL, if it is missing, or not registered in the application's
     * configuration file, the registration fails. If the resource server sends its credentials in the
     * request, the server will try to find a matching registration instead of creating a new one.
     *
     * If the base URL is otherwise already registered, the server responds with a relevant error. In
     * any other case, a new registration is created.
     */
    @PostMapping
    fun handleRegistration(@RequestBody request: ResourceServerRequest): ResponseEntity<ResourceServer> {
        if (request.baseUrl == null || request.baseUrl !in config.urls) {
            badRequest("unknown_base_url")
        }

        if (request.id != null && request.secret != null) {
            val server = resourceServerService.getServerById(request.id)
            if (server != null && server.secret == request.secret && server.scope == request.scope) {
                return ResponseEntity.ok(server)
            } else {
                badRequest("no_matching_registration")
            }
        }

        if (resourceServerService.serverExistsByUrl(request.baseUrl)) {
            badRequest("resource_server_already_registered")
        }

        val server = resourceServerService.createServer(request.baseUrl, request.scope)
        return ResponseEntity.ok(server)
    }

    /**
     * Create and transfer a user authentication token using redirections.
     *
     * Given a valid server ID and an authenticated user, create a URL encoded JSON Web Token as a proof of
     * authentication and redirect the user back to the calling resource server using its base URL and an
     * optional redirect path.
     *
     * The request fails and an error page is displayed if the server ID is invalid.
     */
    @GetMapping("/user")
    fun retrieveCurrentUser(
        @RequestParam("server_id") serverId: String?,
        @RequestParam("redirect_path") redirectPath: String?,
        @AuthenticationPrincipal user: User,
        model: Model
    ): String {
        val server = resourceServerService.getServerById(serverId)
        if (server == null) {
            model.addAttribute("errorType", "invalid_resource_server_id")
            return "generic-error"
        }

        val token = resourceServerService.createEncodedUserToken(server, user)
        val url = UriComponentsBuilder
            .fromUriString(server.baseUrl + (redirectPath ?: ""))
            .queryParam("token", token)
            .toUriString()

        return "redirect:${url}"
    }

    /**
     * Validate a given user authentication token in the context of the authenticated resource server.
     *
     * If the resource server authentication fails, or the token is invalid in the context of the resource
     * server, a 401 response is generated with a relevant message. If the user contained in the token no
     * longer exists, a 400 response is created.
     *
     * In any other case, the username of the user corresponding to the token is returned in a JSON object.
     */
    @PostMapping("/user/validate")
    @ResponseBody
    fun validateUserToken(
        @RequestHeader("Authorization") header: String?,
        @RequestBody token: String?
    ): ResponseEntity<Map<String, String>> {
        val server = resourceServerService.authenticateBasic(header)
            ?: unauthorized("unknown_resource_server")

        if (token == null || !resourceServerService.isUserTokenValid(server, token))
            unauthorized("invalid_token")

        val user = resourceServerService.getUserFromUserToken(server, token)
            ?: badRequest("invalid_user")

        return ResponseEntity.ok(user.mapOfUsername())
    }
}
