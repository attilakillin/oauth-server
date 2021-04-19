package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.validators.AuthValidator
import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.repositories.TransientRepository
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

@Controller
@RequestMapping("/authorize")
class AuthorizationController (
    private val authValidator: AuthValidator,
    private val clientRepository: ClientRepository,
    private val transientRepository: TransientRepository
) {
    private val requests = mutableMapOf<String, AuthRequest>()

    @GetMapping
    fun authorizationRequested(@RequestParam params: Map<String, String>, model: Model): String {
        val request = jacksonObjectMapper().convertValue<AuthRequest>(params)
        authValidator.ifShouldReject(request, clientRepository)?.let {
            return errorPageWithReason(model, it)
        }

        val client = clientRepository.findById(request.clientId!!).get()

        /* If it's null, but passed validation, then the client only has one redirect URI. */
        request.redirectUri = request.redirectUri ?: client.redirectUris.first()

        if (request.responseType !in client.responseTypes)
            return redirectWithError(request.redirectUri!!, "unsupported_response_type")

        if (request.scope.isEmpty())
            request.scope.addAll(client.scope)

        if (authValidator.shouldRejectScope(request.scope, client))
            return redirectWithError(request.redirectUri!!, "invalid_scope")

        val reqId = RandomString.generate(8)
        requests[reqId] = request

        model.addAllAttributes(mapOf("reqId" to reqId, "scope" to request.scope))
        model.addClientAttributes(client)
        return "auth_approve"
    }

    @PostMapping("/approve")
    fun approveAuthorization(@RequestParam params: Map<String, String>, model: Model): String {
        val request = requests.remove(params["reqId"])
            ?: return errorPageWithReason(model, "No matching authorization request!")

        if (params["approve"] == null)
            return redirectWithError(request.redirectUri!!,"access_denied")

        val scope = getScopeFrom(params)
        scope.forEach {
            if (it !in request.scope)
                return redirectWithError(request.redirectUri!!, "invalid_scope")
        }
        request.scope = scope

        return when (request.responseType) {
            "code" -> handleCodeResponse(request)
            else -> redirectWithError(request.redirectUri!!, "unsupported_response_type")
        }
    }

    private fun Model.addClientAttributes(client: Client) {
        this.addAttribute("client_name", client.extraInfo["client_name"])
    }

    private fun buildURL(base: String, params: Map<String, String?>): String {
        val builder = UriComponentsBuilder.fromUriString(base)
        params.forEach { (key, value) -> if (value != null) builder.queryParam(key, value) }
        return builder.toUriString()
    }

    private fun errorPageWithReason(model: Model, reason: String): String {
        model.addAttribute("reason", reason)
        return "auth_error"
    }

    private fun redirectWithError(redirectUri: String, error: String): String {
        return "redirect:" + buildURL(redirectUri, mapOf("error" to error))
    }

    private fun getScopeFrom(params: Map<String, String>): MutableSet<String> {
        return params.asSequence()
                     .filter { it.key.startsWith("scope_") }.distinct()
                     .map { it.key.removePrefix("scope_") }
                     .toMutableSet()
    }

    private fun handleCodeResponse(request: AuthRequest): String {
        val code = RandomString.generate(16)
        transientRepository.saveAuthCode(AuthCode.fromRequest(code, request, 60))
        // TODO Lifetime should be a constant somewhere

        return "redirect:" + buildURL(request.redirectUri!!,
            mapOf("code" to code, "state" to request.state))
    }
}