package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.validators.AuthValidator
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

    /** Displays the "approve authorization" page to the caller, or, in case of an error,
     *  it either redirects to the client or displays an error page to the caller. */
    @GetMapping
    fun authorizationRequested(@RequestParam params: Map<String, String>, model: Model): String {
        val unvalidatedRequest = jacksonObjectMapper().convertValue<UnvalidatedAuthRequest>(params)

        authValidator.validateSensitiveOrError(unvalidatedRequest)?.let { message ->
            return errorPageWithReason(model, message)
        }
        authValidator.validateAdditionalOrError(unvalidatedRequest)?.let { message ->
            return redirectWithError(unvalidatedRequest.redirectUri!!, message)
        }
        val request = authValidator.convertToValidRequest(unvalidatedRequest)

        val reqId = RandomString.generateUntil(8) { it !in requests.keys }
        requests[reqId] = request

        model.addAllAttributes(mapOf("reqId" to reqId, "scope" to request.scope))
        model.addClientAttributes(request.clientId)
        return "auth_approve"
    }

    /** Approved authorization forms are processed here. If the authorization request passes
     *  validation, the function redirects the caller to the client, otherwise it displays
     *  an error page to the caller (or redirects to the client with an error code depending on the
     *  error itself). */
    @PostMapping("/approve")
    fun approveAuthorization(@RequestParam params: Map<String, String>, model: Model): String {
        val request = requests.remove(params["reqId"])
            ?: return errorPageWithReason(model, "No matching authorization request!")

        if (params["approve"] == null)
            return redirectWithError(request.redirectUri,"access_denied")

        val scope = getScopeFrom(params)
        scope.forEach {
            if (it !in request.scope) return redirectWithError(request.redirectUri, "invalid_scope")
        }
        request.scope = scope

        return when (request.responseType) {
            "code" -> handleCodeResponse(request)
            else -> redirectWithError(request.redirectUri, "unsupported_response_type")
        }
    }

    /** Adds every client attribute to the model that is required for the authorization prompt. */
    private fun Model.addClientAttributes(id: String) {
        val client = clientRepository.findById(id).get()
        this.addAttribute("client_name", client.extraData["client_name"])
    }

    /** Creates an URL string from a [base] with the given [params]. If a parameter has a null
     *  value, the corresponding key is skipped. */
    private fun buildURL(base: String, params: Map<String, String?>): String {
        val builder = UriComponentsBuilder.fromUriString(base)
        params.forEach { (key, value) -> if (value != null) builder.queryParam(key, value) }
        return builder.toUriString()
    }

    /** Sets the error [reason] and returns a string pointing to the authorization error page. */
    private fun errorPageWithReason(model: Model, reason: String): String {
        model.addAttribute("reason", reason)
        return "auth_error"
    }

    /** Returns a string pointing to the given [redirectUri] with the given [error] as a query param. */
    private fun redirectWithError(redirectUri: String, error: String): String {
        return "redirect:" + buildURL(redirectUri, mapOf("error" to error))
    }

    /** Extracts the scope values from a map where they are prefixed with "scope_". */
    private fun getScopeFrom(params: Map<String, String>): MutableSet<String> {
        return params.asSequence()
                     .filter { it.key.startsWith("scope_") }.distinct()
                     .map { it.key.removePrefix("scope_") }
                     .toMutableSet()
    }

    /** Handles the authorization code response. */
    private fun handleCodeResponse(request: AuthRequest): String {
        val code = RandomString.generate(16)
        transientRepository.saveAuthCode(AuthCode.fromRequest(code, request, 60))
        // TODO Lifetime should be a constant somewhere

        return "redirect:" + buildURL(request.redirectUri, mapOf("code" to code, "state" to request.state))
    }
}