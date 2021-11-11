package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthRequest
import com.bme.jnsbbk.oauthserver.authorization.entities.UnvalidatedAuthRequest
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.config.ServerMetadata
import com.bme.jnsbbk.oauthserver.token.TokenFactory
import com.bme.jnsbbk.oauthserver.token.TokenRepository
import com.bme.jnsbbk.oauthserver.user.UserRepository
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder
import java.security.Principal

@Controller
@RequestMapping(ServerMetadata.Endpoints.authorization)
class AuthorizationController(
    private val authRequestService: AuthRequestService,
    private val clientRepository: ClientRepository,
    private val authCodeRepository: AuthCodeRepository,
    private val authCodeFactory: AuthCodeFactory,
    private val tokenFactory: TokenFactory,
    private val tokenRepository: TokenRepository,
    private val userRepository: UserRepository
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

        val (senValid, senMessage) = authRequestService.isSensitiveInfoValid(request)
        if (!senValid) return errorPageWithReason(model, senMessage)

        val (addValid, addMessage) = authRequestService.isAdditionalInfoValid(request)
        if (!addValid) return redirectWithError(request.redirectUri!!, addMessage)

        val validRequest = authRequestService.convertToValidRequest(request)

        val reqId = RandomString.generateUntil(8) { it !in requests.keys }
        requests[reqId] = validRequest

        model.addAllAttributes(mapOf("reqId" to reqId, "scope" to validRequest.scope))
        model.addClientAttributes(validRequest.clientId)
        return "auth-form"
    }

    /**
     * Processes authorization forms sent by the user.
     *
     * If the authorization passes validation, the function redirects the caller to the client,
     * otherwise it either displays an error page in place,or redirects the user to the client.
     */
    @PostMapping
    fun approveAuthorization(
        @RequestParam params: Map<String, String>,
        model: Model,
        principal: Principal
    ): String {
        val request = requests.remove(params["reqId"])
            ?: return errorPageWithReason(model, "No matching authorization request!")

        request.userId = userRepository.findByUsername(principal.name)?.id
            ?: return errorPageWithReason(model, "User authentication failed!")

        return when (request.responseType) {
            "code" -> handleCodeResponse(params, request)
            "token" -> handleTokenResponse(params, request)
            else -> redirectWithError(request.redirectUri, "unsupported_response_type")
        }
    }

    /** Handles responses to valid authorization code requests. */
    private fun handleCodeResponse(params: Map<String, String>, request: AuthRequest): String {
        if (params["approve"] == null)
            return redirectWithError(request.redirectUri, "access_denied")

        val scope = getScopeFrom(params)
        scope.forEach {
            if (it !in request.scope) return redirectWithError(request.redirectUri, "invalid_scope")
        }
        request.scope = scope

        val code = RandomString.generateUntil(16) { !authCodeRepository.existsById(it) }
        authCodeRepository.save(authCodeFactory.fromRequest(code, request))

        return "redirect:" +
                buildUrlWithParams(request.redirectUri, mapOf("code" to code, "state" to request.state))
    }

    private fun handleTokenResponse(params: Map<String, String>, request: AuthRequest): String {
        if (params["approve"] == null)
            return redirectWithError(request.redirectUri, "access_denied", true)

        val scope = getScopeFrom(params)
        scope.forEach {
            if (it !in request.scope)
                return redirectWithError(request.redirectUri, "invalid_scope", true)
        }
        request.scope = scope

        val code = authCodeFactory.fromRequest("irrelevant", request)
        val access = tokenFactory.accessFromCode(
            RandomString.generateUntil { !tokenRepository.existsById(it) },
            code
        )
        tokenRepository.save(access)
        val response = tokenFactory.responseJwtFromTokens(access, null)

        val responseMap = mapOf(
            "access_token" to response.accessToken,
            "token_type" to response.tokenType,
            "scope" to response.scope.joinToString(" "),
            "state" to request.state
        )

        return "redirect:" +
                buildUrlWithFragment(request.redirectUri, responseMap)
    }

    private fun Model.addClientAttributes(id: String) {
        val client = clientRepository.findById(id).get()
        this.addAttribute("client_name", client.extraData["client_name"])
    }

    private fun buildUrlWithParams(base: String, params: Map<String, String?>): String {
        val builder = UriComponentsBuilder.fromUriString(base)
        params.forEach { (key, value) -> if (value != null) builder.queryParam(key, value) }
        return builder.toUriString()
    }

    private fun buildUrlWithFragment(base: String, params: Map<String, String?>): String {
        val fragmentBuilder = UriComponentsBuilder.fromUriString("")
        params.forEach { (key, value) -> if (value != null) fragmentBuilder.queryParam(key, value) }
        return UriComponentsBuilder
            .fromUriString(base)
            .fragment(fragmentBuilder.toUriString().removePrefix("?"))
            .toUriString()
    }

    private fun errorPageWithReason(model: Model, reason: String): String {
        model.addAttribute("reason", reason)
        return "auth_error"
    }

    private fun redirectWithError(redirectUri: String, error: String, inFragment: Boolean = false): String {
        return if (inFragment)
            "redirect:" + buildUrlWithFragment(redirectUri, mapOf("error" to error))
        else
            "redirect:" + buildUrlWithParams(redirectUri, mapOf("error" to error))
    }

    private fun getScopeFrom(params: Map<String, String>): Set<String> {
        return params
            .asSequence()
            .filter { it.key.startsWith("scope_") }
            .distinct()
            .map { it.key.removePrefix("scope_") }
            .toSet()
    }

}
