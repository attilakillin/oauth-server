package com.bme.jnsbbk.resourceserver.authentication

import com.bme.jnsbbk.resourceserver.configuration.AppConfig
import com.bme.jnsbbk.resourceserver.configuration.Property
import com.bme.jnsbbk.resourceserver.configuration.PropertyRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthenticationInterceptor(
    private val propertyRepository: PropertyRepository,
    appConfig: AppConfig
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val token: String? = request.getParameter("token")

        if (token == null) {
            response.sendRedirect(userTokenRequestUrl)
            return false
        }

        val validation = sendValidationQuery(token)

        if (validation == null) {
            response.sendRedirect("login-error")
            return false
        }

        request.setAttribute("username", validation["username"])
        return true
    }

    private val auth = appConfig.authorizationServer

    private val userTokenRequestUrl: String
        get() {
            val id = propertyRepository.findByIdOrNull(Property.Key.ID)!!.value

            return UriComponentsBuilder
                .fromUriString(auth.url + auth.endpoints.userTokenRequest)
                .queryParam("server_id", id)
                .toUriString()
        }

    private fun sendValidationQuery(tokenParam: String): Map<String, String>? {
        val token = try {
            Base64.getUrlDecoder().decode(tokenParam).toString(Charsets.UTF_8)
        } catch (e: Exception) {
            tokenParam
        }

        val id = propertyRepository.findByIdOrNull(Property.Key.ID)!!.value
        val secret = propertyRepository.findByIdOrNull(Property.Key.SECRET)!!.value

        val headers = HttpHeaders().apply { setBasicAuth(id, secret) }
        val request = HttpEntity(token, headers)

        val url = auth.url + auth.endpoints.userTokenValidation

        return try {
            RestTemplate().postForEntity<Map<String, String>>(url, request).body
        } catch (e: Exception) {
            null
        }
    }
}