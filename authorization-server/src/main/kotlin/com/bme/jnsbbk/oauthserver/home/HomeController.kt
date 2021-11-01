package com.bme.jnsbbk.oauthserver.home

import com.bme.jnsbbk.oauthserver.token.TokenRepository
import com.bme.jnsbbk.oauthserver.token.entities.isTimestampValid
import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.user.entities.UserInfo
import com.bme.jnsbbk.oauthserver.user.entities.fromNullable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class HomeController(
    private val userService: UserService,
    private val tokenRepository: TokenRepository
) {

    @GetMapping("/")
    fun redirectToHome(): String = "redirect:/home"

    @GetMapping("/home")
    fun getHomeRoot(@AuthenticationPrincipal user: User, model: Model): String {
        model.addAttribute("username", user.username)
        return "home-root"
    }

    @GetMapping("/home/userinfo")
    fun getUserInfo(@AuthenticationPrincipal user: User, model: Model): String {
        model.addAttribute("userinfo", user.info)
        return "home-userinfo"
    }

    @PostMapping("/home/userinfo")
    fun postUserInfo(
        @RequestParam("name") name: String?,
        @RequestParam("email") email: String?,
        @RequestParam("address") address: String?,
        @AuthenticationPrincipal user: User,
        model: Model
    ): String {
        user.info = UserInfo.fromNullable(name, email, address)
        userService.updateUser(user)

        model.addAttribute("userinfo", user.info)
        model.addAttribute("success", true)
        return "home-userinfo"
    }

    @GetMapping("/home/authorizations")
    fun getAuthorizations(@AuthenticationPrincipal principal: User, model: Model): String {
        fillModelWithTokens(principal.id, model)
        return "home-authorizations"
    }

    @PostMapping("/home/authorizations/revoke")
    fun revokeToken(
        @AuthenticationPrincipal principal: User,
        @RequestParam("token") tokenId: String,
        model: Model
    ): String {
        val token = tokenRepository.findByIdOrNull(tokenId)
        if (token != null && token.userId == principal.id) {
            tokenRepository.deleteById(tokenId)
            model.addAttribute("revoke_success", true)
        } else {
            model.addAttribute("revoke_failure", true)
        }

        fillModelWithTokens(principal.id, model)
        return "home-authorizations"
    }

    private fun fillModelWithTokens(userId: String, model: Model) {
        val tokens = tokenRepository.findAllByUserId(userId).map {
            TokenRow(
                value = it.value,
                type = it.type.name,
                clientId = it.clientId,
                scope = it.scope.joinToString(" "),
                active = if (it.isTimestampValid()) "Yes" else "No"
            )
        }

        model.addAttribute("tokens", tokens)
    }

    data class TokenRow(
        val value: String,
        val type: String,
        val clientId: String,
        val scope: String,
        val active: String
    )
}
