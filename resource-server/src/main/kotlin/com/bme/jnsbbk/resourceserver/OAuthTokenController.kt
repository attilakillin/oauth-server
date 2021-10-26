package com.bme.jnsbbk.resourceserver

import com.bme.jnsbbk.resourceserver.configuration.AppConfig
import com.bme.jnsbbk.resourceserver.configuration.Property
import com.bme.jnsbbk.resourceserver.configuration.PropertyRepository
import com.bme.jnsbbk.resourceserver.resources.UserData
import com.bme.jnsbbk.resourceserver.resources.UserDataRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Controller
@RequestMapping("/oauth/token")
class OAuthTokenController(
    private val propertyRepository: PropertyRepository,
    private val userDataRepository: UserDataRepository,
    private val appConfig: AppConfig
) {

    @GetMapping
    fun handleRequest(@RequestParam token: String?): ResponseEntity<UserData> {
        if (token == null) return ResponseEntity.badRequest().build()

        val response = introspectToken(token)
        if (response == null || response.active == "false") return ResponseEntity.status(401).build()

        val userData = userDataRepository.findByIdOrNull(response.username)

        // TODO Scope validation

        return ResponseEntity.ok(userData)
    }

    private data class IntrospectResponse(
        val active: String,
        val iss: String,
        val sub: String,
        val scope: String,
        val username: String
    )

    private fun introspectToken(token: String): IntrospectResponse? {
        val id = propertyRepository.findByIdOrNull(Property.Key.ID)!!.value
        val secret = propertyRepository.findByIdOrNull(Property.Key.SECRET)!!.value

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBasicAuth(id, secret)
        }
        val request = HttpEntity(mapOf("token" to token), headers)

        val auth = appConfig.authorizationServer
        val url = auth.url + auth.endpoints.tokenIntrospection

        return try {
            RestTemplate().postForEntity<IntrospectResponse>(url, request).body
        } catch (e: Exception) {
            null
        }
    }
}
