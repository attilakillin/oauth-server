package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import com.bme.jnsbbk.oauthserver.wellknown.ServerMetadata
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.bme.jnsbbk.oauthserver.utils.URL
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping(ServerMetadata.Endpoints.authorization)
class AuthorizationController(
    private val authorizationService: AuthorizationService
) {
    private val requests = mutableMapOf<String, AuthRequest>()

    /**
     * Displays the "approve authorization" page to the caller.
     *
     * In case of an error, it either redirects the user to the client
     * or displays an error page in place.
     */
    @GetMapping
    fun authorizationRequested(@RequestParam params: Map<String, String>, model: Model): String {
        val request = jacksonObjectMapper().convertValue<UnvalidatedAuthRequest>(params)

        if (!authorizationService.validateRequestClient(request)) {
            model.addAttribute("errorType", "auth_invalid_client")
            return "generic-error"
        }
        if (!authorizationService.validateRequestUri(request)) {
            model.addAttribute("errorType", "auth_invalid_uri")
            return "generic-error"
        }

        val url = URL(request.redirectUri ?: authorizationService.getClientFrom(request).redirectUris.first())
        if (!authorizationService.validateRequestResponseType(request)) {
            return url.withParam("error", "unsupported_response_type").redirect()
        }
        if (!authorizationService.validateRequestScope(request)) {
            return url.withParam("error", "invalid_scope").redirect()
        }

        val authRequest = authorizationService.convertToValidRequest(request)
        val requestId = RandomString.generateUntil(8) { it !in requests.keys }
        requests[requestId] = authRequest

        model.addAttribute("reqId", requestId)
        model.addAttribute("scope", authRequest.scope)
        model.addAttribute("clientId", authRequest.clientId)
        return "auth-form"
    }

    /**
     * Processes authorization forms sent by the user.
     *
     * If the authorization passes validation, the function redirects the caller to the client,
     * otherwise it either displays an error page in place, or redirects the user to the client.
     */
    @PostMapping
    fun approveAuthorization(
        @RequestParam params: Map<String, String>,
        @AuthenticationPrincipal user: User,
        model: Model
    ): String {
        val request = requests.remove(params["reqId"])
        if (request == null) {
            model.addAttribute("errorType", "auth_no_matching_request")
            return "generic-error"
        }

        request.userId = user.id
        val url = URL(request.redirectUri)

        return when (request.responseType) {
            "code" -> handleCodeResponse(request, params)
            "token" -> handleTokenResponse(request, params)
            else -> url.withParam("error", "unsupported_response_type").redirect()
        }
    }

    /**
     * Handles responses to valid authorization code requests.
     *
     * If the request is not approved, or contains an invalid scope, the function returns a redirection
     * string with an error message appended. Otherwise, it saves the authorization code and redirects
     * to the client with the code as a query parameter.
     */
    private fun handleCodeResponse(request: AuthRequest, params: Map<String, String>): String {
        val url = URL(request.redirectUri)
        if (params["approve"] == null) {
            return url.withParam("error", "access_denied").redirect()
        }

        val scope = authorizationService.extractPrefixedScopes(params, "scope_")
        if (scope.any { it !in request.scope }) {
            return url.withParam("error", "invalid_scope").redirect()
        }
        request.scope = scope

        val code = authorizationService.createAuthCode(request)

        return url.withParams(mapOf("code" to code.value, "state" to request.state)).redirect()
    }

    /**
     * Handles responses to token requests of the implicit flow.
     *
     * If the request is not approved, or contains an invalid scope, the function returns a redirection
     * string with an error message appended as a URI fragment. Otherwise, it creates an access token and
     * redirects to the client with the token and all necessary parameters as fragment variables.
     */
    private fun handleTokenResponse(request: AuthRequest, params: Map<String, String>): String {
        val url = URL(request.redirectUri)
        if (params["approve"] == null) {
            return url.withFragment("error", "access_denied").redirect()
        }

        val scope = authorizationService.extractPrefixedScopes(params, "scope_")
        if (scope.any { it !in request.scope }) {
            return url.withFragment("error", "invalid_scope").redirect()
        }
        request.scope = scope

        val response = authorizationService.createImplicitResponse(request)
        return url.withFragments(response).redirect()
    }
}
