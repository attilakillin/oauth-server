package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.validators.AuthValidator
import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.repositories.TransientRepository
import com.bme.jnsbbk.oauthserver.utils.RandomString
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
    private val requests = mutableMapOf<String, MutableMap<String, String>>()

    @GetMapping
    fun authorizationRequested(@RequestParam params: MutableMap<String, String>, model: Model): String {
        authValidator.ifShouldReject(params, clientRepository)?.let {
            return errorPageWithReason(model, it)
        }

        val client = clientRepository.findById(params["client_id"]!!).get()

        /* If it's null, but passed validation, then the client only has one redirect URI. We'll use that. */
        if (params["redirect_uri"] == null) params["redirect_uri"] = client.redirectUris.first()
        val redirectUri = params["redirect_uri"]!!

        if (params["response_type"] !in client.responseTypes)
            return redirectWithError(redirectUri, "unsupported_response_type")

        var scope = params["scope"]?.split(' ')?.toSet()
        if (scope == null) scope = client.scope

        if (authValidator.shouldRejectScope(scope, client))
            return redirectWithError(redirectUri, "invalid_scope")

        val reqId = RandomString.generate(8)
        requests[reqId] = params

        model.addAllAttributes(mapOf("reqId" to reqId, "scope" to scope))
        model.addClientAttributes(client)
        return "auth_approve"
    }

    @PostMapping("/approve")
    fun approveAuthorization(@RequestParam params: Map<String, String>, model: Model): String {
        val query = requests.remove(params["reqId"])
            ?: return errorPageWithReason(model, "No matching authorization request!")

        val redirectUri = query["redirect_uri"]!!

        if (params["approve"] == null)
            return redirectWithError(redirectUri,"access_denied")

        val client = clientRepository.findById(query["client_id"]!!).get()

        val scope = getScopeFrom(params)
        scope.split(" ").forEach {
            if (it !in client.scope) return redirectWithError(redirectUri, "invalid_scope")
        }
        query["scope"] = scope

        return when (query["response_type"]) {
            "code" -> handleCodeResponse(redirectUri, query)
            else -> redirectWithError(redirectUri, "unsupported_response_type")
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

    private fun getScopeFrom(params: Map<String, String>): String {
        return params.asSequence()
                     .filter { it.key.startsWith("scope_") }.distinct()
                     .map { it.key.removePrefix("scope_") }
                     .joinToString(" ")
    }

    private fun handleCodeResponse(redirectUri: String, query: Map<String, String>): String {
        val code = RandomString.generate(16)
        transientRepository.authCodes[code] = query

        return "redirect:" + buildURL(redirectUri, mapOf("code" to code, "state" to query["state"]))
    }
}