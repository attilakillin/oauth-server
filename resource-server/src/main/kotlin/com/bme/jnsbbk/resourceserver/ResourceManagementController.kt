package com.bme.jnsbbk.resourceserver

import com.bme.jnsbbk.resourceserver.configuration.AppConfig
import com.bme.jnsbbk.resourceserver.configuration.Property
import com.bme.jnsbbk.resourceserver.configuration.PropertyRepository
import com.bme.jnsbbk.resourceserver.resources.UserData
import com.bme.jnsbbk.resourceserver.resources.UserDataRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@Controller
class ResourceManagementController(
    val appConfig: AppConfig,
    val propertyRepository: PropertyRepository,
    val userDataRepository: UserDataRepository
) {

    @GetMapping
    fun handleRequest(
        @RequestParam("token") token: String?,
        model: Model
    ): String {
        if (token == null) return "redirect:" + getUserTokenRequestUrl()

        val response = validateToken(token)

        if (response == null) {
            model.addAttribute("url", getUserTokenRequestUrl())
            return "login-error"
        }

        val userData = userDataRepository.findByIdOrNull(response.username)
            ?: userDataRepository.save(UserData(response.username, null, null))
        model.addAttribute("user", userData)
        model.addAttribute("token", token)
        return "resource-content"
    }

    @PostMapping
    fun handleSave(
        @RequestParam("token") token: String?,
        @RequestParam("username") username: String?,
        @RequestParam data: Map<String, String>,
        model: Model
    ): String {
        if (token == null || username == null) return "redirect:" + getUserTokenRequestUrl()

        val response = validateToken(token)

        if (response == null || response.username != username) {
            model.addAttribute("url", getUserTokenRequestUrl())
            return "login-error"
        }

        val userData = UserData(username, data["email"], data["address"])
        userDataRepository.save(userData)

        model.addAttribute("user", userData)
        model.addAttribute("token", token)
        model.addAttribute("success", true)
        return "resource-content"
    }

    private data class Response(
        val username: String
    )

    private fun getUserTokenRequestUrl(): String {
        val id = propertyRepository.findById(Property.Key.ID).get().value
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

        val id = propertyRepository.findById(Property.Key.ID).get().value
        val secret = propertyRepository.findById(Property.Key.SECRET).get().value

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
