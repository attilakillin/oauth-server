package com.bme.jnsbbk.oauthserver.user

import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.forbidden
import com.bme.jnsbbk.oauthserver.jwt.TokenJwtHandler
import com.bme.jnsbbk.oauthserver.token.TokenRepository
import com.bme.jnsbbk.oauthserver.token.entities.isTimestampValid
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/oauth/openid/userinfo")
class UserInfoController(
    private val userService: UserService,
    private val tokenJwtHandler: TokenJwtHandler,
    private val tokenRepository: TokenRepository
) {

    @GetMapping
    fun handleRequest(@RequestParam("token") jwt: String): ResponseEntity<Map<String, String>> {
        val id = tokenJwtHandler.getValidTokenId(jwt)  ?: badRequest()
        val token = tokenRepository.findAccessById(id) ?: badRequest()
        if (!token.isTimestampValid()) badRequest()

        if ("openid" !in token.scope) forbidden()

        val user = userService.getUserById(token.userId) ?: return ResponseEntity.notFound().build()

        val claims = mutableMapOf("sub" to user.id)
        if ("profile" in token.scope) claims["name"] = user.info.name
        if ("email" in token.scope) claims["email"] = user.info.email
        if ("address" in token.scope) claims["address"] = user.info.address

        return ResponseEntity.ok(claims)
    }
}
