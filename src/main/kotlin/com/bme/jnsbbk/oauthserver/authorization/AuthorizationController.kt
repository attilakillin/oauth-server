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
    private val requests = mutableMapOf<String, Map<String, String>>()

    @GetMapping
    fun authorizationRequested(@RequestParam params: MutableMap<String, String>, model: Model): String {
        val rejection = authValidator.shouldRejectRequest(params, clientRepository)
        if (rejection.isPresent) {
            model.addAttribute("reason", rejection.get())
            return "auth_error"
        }

        val client = clientRepository.findById(params["client_id"]!!).get()

        /* If it's null, but passed validation, then the client only has one redirect URI. We'll use that. */
        if (params["redirect_uri"] == null)
            params["redirect_uri"] = client.redirectUris.first()

        val reqid = RandomString.generate(8)
        requests[reqid] = params

        model.addAttribute("reqid", reqid)
        addClientAttributes(client, model)
        return "auth_approve"
    }

    @PostMapping("/approve")
    fun approveAuthorization(@RequestParam("reqid") reqid: String,
                             @RequestParam("approve") approve: String?, model: Model): String {
        val query = requests.remove(reqid)

        if (query == null) {
            model.addAttribute("reason", "No matching authorization request!")
            return "auth_error"
        }

        val redirectUri = query["redirect_uri"]!!

        if (approve == null)
            return "redirect:" + buildURL(redirectUri, mapOf("error" to "access_denied"))

        // Note: refactor here for multiple response types
        if (query["response_type"] != "code")
            return "redirect:" + buildURL(redirectUri, mapOf("error" to "unsupported_response_type"))

        val code = RandomString.generate(16)

        transientRepository.authCodes[code] = query

        return "redirect:" + buildURL(redirectUri, mapOf("code" to code, "state" to query["state"]))
    }

    private fun addClientAttributes(client: Client, model: Model) {
        model.addAttribute("client_name", client.extraInfo["client_name"])
    }

    private fun buildURL(base: String, params: Map<String, String?>): String {
        val builder = UriComponentsBuilder.fromUriString(base)
        params.forEach { key, value -> if (value != null) builder.queryParam(key, value) }
        return builder.toUriString()
    }
}