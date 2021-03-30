package com.bme.jnsbbk.oauthserver.authorization

import com.bme.jnsbbk.oauthserver.authorization.validators.AuthValidator
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class AuthorizationController (
    private val authValidator: AuthValidator,
    private val clientRepository: ClientRepository
) {

    @GetMapping("/authorize")
    fun authorizationRequested(@RequestParam("client_id") clientId: String,
                               @RequestParam("redirect_uri") redirectUri: String,
                               @RequestParam state: String?, model: Model): String {
        val rejectionResult = authValidator.shouldRejectRequest(clientId, redirectUri, clientRepository)
        if (rejectionResult.isPresent) {
            model.addAttribute("reason", rejectionResult.get())
            return "auth_error"
        }

        addClientAttributes(clientId, model)
        return "auth_approve"
    }

    private fun addClientAttributes(clientId: String, model: Model) {
        val client = clientRepository.findById(clientId).get()

        model.addAttribute("client_name", client.extraInfo["client_name"])
    }
}