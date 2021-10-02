package com.bme.jnsbbk.resourceserver

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@Controller
class ResourceManagementController(
    val appConfig: AppConfig,
    val propertyRepository: PropertyRepository
) {

    @GetMapping
    fun handleRequest(
        @RequestParam("token") token: String?,
        model: Model
    ): String {
        if (token == null) return "redirect:" + getUserTokenRequestUrl()

        val data = validateToken(token)

        if (data == null) {
            model.addAttribute("url", getUserTokenRequestUrl())
            return "login-error"
        }

        model.addAttribute("username", data.username)
        return "resource-content"
    }

    private data class Response(
        val username: String
    )

    private fun getUserTokenRequestUrl(): String {
        val id = propertyRepository.findById(Key.ID).get().value
        val auth = appConfig.authorizationServer

        return UriComponentsBuilder
            .fromUriString(auth.url + auth.endpoints.userTokenRequest)
            .queryParam("server_id", id)
            .toUriString()
    }

    private fun validateToken(base64Token: String?): Response? {
        val token: String
        try {
            token = Base64.getUrlDecoder().decode(base64Token).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            return null
        }

        val id = propertyRepository.findById(Key.ID).get().value
        val secret = propertyRepository.findById(Key.SECRET).get().value

        val headers = HttpHeaders().apply { setBasicAuth(id, secret) }
        val request = HttpEntity(token, headers)

        val auth = appConfig.authorizationServer
        val url = auth.url + auth.endpoints.userTokenValidation

        return try {
            val result = RestTemplate().postForEntity<Response>(url, request)
            result.body
        } catch (e: Exception) {
            null
        }
    }
}
