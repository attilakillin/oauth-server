package com.bme.jnsbbk.resourceserver.authentication

import com.bme.jnsbbk.resourceserver.configuration.AppConfig
import com.bme.jnsbbk.resourceserver.configuration.Property
import com.bme.jnsbbk.resourceserver.configuration.PropertyService
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Intercepts requests and validates a user authentication token present in them.
 *
 * If no such token can be found, it redirects the caller to the login page of the auth server.
 */
class AuthenticationInterceptor(
    private val propertyService: PropertyService,
    private val config: AppConfig.AuthorizationServer
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val token: String? = request.getParameter("token")

        if (token == null) {
            response.sendRedirect(userTokenRequestUrl)
            return false
        }

        val username = validateTokenAndRetrieveUsername(token)

        if (username == null) {
            response.sendRedirect("login-error")
            return false
        }

        request.setAttribute("username", username)
        return true
    }

    /** Shorthand property for getting the user token request URL of the auth server. */
    private val userTokenRequestUrl: String
        get() {
            val id = propertyService.getProperty(Property.Key.ID)

            return UriComponentsBuilder
                .fromUriString(config.url + config.endpoints.userTokenRequest)
                .queryParam("server_id", id)
                .toUriString()
        }

    /** Validates a given token and return sthe name of the user associated with it, or null if an error occurs. */
    private fun validateTokenAndRetrieveUsername(tokenParam: String): String? {
        val token = try {
            Base64.getUrlDecoder().decode(tokenParam).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            tokenParam
        }

        val id = propertyService.getProperty(Property.Key.ID) ?: return null
        val secret = propertyService.getProperty(Property.Key.SECRET) ?: return null

        val headers = HttpHeaders().apply { setBasicAuth(id, secret) }
        val request = HttpEntity(token, headers)

        val url = config.url + config.endpoints.userTokenValidation

        return try {
            RestTemplate().postForEntity<Map<String, String>>(url, request).body?.get("username")
        } catch (e: Exception) {
            null
        }
    }
}
