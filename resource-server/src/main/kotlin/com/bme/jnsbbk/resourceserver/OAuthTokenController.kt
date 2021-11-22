package com.bme.jnsbbk.resourceserver

import com.bme.jnsbbk.resourceserver.configuration.AppConfig
import com.bme.jnsbbk.resourceserver.configuration.Property
import com.bme.jnsbbk.resourceserver.configuration.PropertyService
import com.bme.jnsbbk.resourceserver.resources.UserData
import com.bme.jnsbbk.resourceserver.resources.UserDataRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Controller
@RequestMapping("/oauth/token")
class OAuthTokenController(
    private val propertyService: PropertyService,
    private val userDataRepository: UserDataRepository,
    private val appConfig: AppConfig
) {

    /** Handles GET requests from clients. */
    @GetMapping
    fun handleQuery(@RequestParam token: String?): ResponseEntity<Map<String, String?>> {
        if (token == null)
            return ResponseEntity.status(400).body(mapOf("error" to "no_token"))

        val response = introspectToken(token)
        if (response == null || response.active == "false")
            return ResponseEntity.status(401).body(mapOf("error" to "invalid_token"))

        val scope = response.scope!!.split(" ")
        if ("read" !in scope)
            return ResponseEntity.status(401).body(mapOf("error" to "invalid_scope"))

        val notes = userDataRepository.findByIdOrNull(response.username)?.notes
        return ResponseEntity.ok(mapOf("username" to response.username, "notes" to notes))
    }

    /** Handles POST requests from clients. */
    @PostMapping
    fun handleUpdate(
        @RequestParam token: String?,
        @RequestParam notes: String?
    ): ResponseEntity<Map<String, String?>> {
        if (token == null || notes == null)
            return ResponseEntity.status(400).body(mapOf("error" to "no_token_or_notes"))

        val response = introspectToken(token)
        if (response == null || response.active == "false")
            return ResponseEntity.status(401).body(mapOf("error" to "invalid_token"))

        val scope = response.scope!!.split(" ")
        if ("write" !in scope)
            return ResponseEntity.status(401).body(mapOf("error" to "invalid_scope"))

        userDataRepository.save(UserData(response.username!!, notes))
        return ResponseEntity.status(204).build()
    }

    /** Data class that wraps the introspection response into a strongly-typed class. */
    private data class IntrospectResponse(
        val active: String,
        val iss: String?,
        val sub: String?,
        val scope: String?,
        val username: String?
    )

    private fun introspectToken(token: String): IntrospectResponse? {
        val id = propertyService.getProperty(Property.Key.ID) ?: return null
        val secret = propertyService.getProperty(Property.Key.SECRET) ?: return null

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
