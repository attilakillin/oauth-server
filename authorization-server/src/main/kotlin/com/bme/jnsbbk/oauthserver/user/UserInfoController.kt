package com.bme.jnsbbk.oauthserver.user

import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.jwt.AccessTokenHandler
import com.bme.jnsbbk.oauthserver.token.entities.isTimestampValid
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/oauth/openid/userinfo")
class UserInfoController(
    private val accessTokenHandler: AccessTokenHandler,
    private val userService: UserService
) {

    /**
     * Responds to OpenID Connect user info requests.
     *
     * If the given JWT access token is valid, and corresponds to a token in the database, respond
     * with information allowed by the scope of the token regarding the subject user.
     *
     * If the token is invalid, or the subject user doesn't exist, the method responds with a 400 message,
     * and if the 'openid' base scope is missing, it responds with a 401 message.
     */
    @GetMapping
    fun handleRequest(
        @RequestHeader("Authorization") header: String?
    ): ResponseEntity<Map<String, String>> {
        val jwt = header?.removePrefix("Bearer ") ?: badRequest("no_token")
        val token = accessTokenHandler.convertToValidToken(jwt) ?: badRequest("invalid_token")
        if (!token.isTimestampValid() || token.userId == null) badRequest("invalid_token")

        if ("openid" !in token.scope) unauthorized("invalid_scope")

        val user = userService.getUserById(token.userId) ?: badRequest("user_not_found")

        val claims = mutableMapOf("sub" to user.id)
        if ("profile" in token.scope) claims["name"] = user.info.name
        if ("email" in token.scope) claims["email"] = user.info.email
        if ("address" in token.scope) claims["address"] = user.info.address

        return ResponseEntity.ok(claims)
    }
}
