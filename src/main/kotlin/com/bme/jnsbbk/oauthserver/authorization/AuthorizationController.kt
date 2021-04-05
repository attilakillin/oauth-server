package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.validators.AuthValidator
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder

@Controller
class AuthorizationController (
    private val authValidator: AuthValidator,
    private val clientRepository: ClientRepository
) {
    private val requests = mutableMapOf<String, Map<String, String>>()

    @GetMapping("/authorize")
    fun authorizationRequested(@RequestParam params: Map<String, String>, model: Model): String {
        val rejectionResult = authValidator.shouldRejectRequest(params, clientRepository)
        if (rejectionResult.isPresent) {
            model.addAttribute("reason", rejectionResult.get())
            return "auth_error"
        }

        val reqid = RandomString.generate(8)
        requests[reqid] = params

        model.addAttribute("reqid", reqid)
        addClientAttributes(params["client_id"]!!, model)
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
            return "redirect:${buildURL(redirectUri, mapOf("error" to "access_denied"))}"

        // Note: refactor here for multiple response types
        if (query["response_type"] != "code")
            return "redirect:${buildURL(redirectUri, mapOf("error" to "unsupported_response_type"))}"

        val code = RandomString.generate(16)

        // TODO Store codes

        return "redirect:${buildURL(redirectUri, mapOf("code" to code, "state" to query["state"]))}"
    }

    private fun addClientAttributes(clientId: String, model: Model) {
        val client = clientRepository.findById(clientId).get()

        model.addAttribute("client_name", client.extraInfo["client_name"])
    }

    private fun buildURL(base: String, params: Map<String, String?>): String {
        val builder = UriComponentsBuilder.fromUriString(base)
        params.forEach { key, value -> if (value != null) builder.queryParam(key, value) }
        return builder.toUriString()
    }
}