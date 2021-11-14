package com.bme.jnsbbk.oauthserver.home

import com.bme.jnsbbk.oauthserver.token.TokenRepository
import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.user.entities.User
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

    /** Root mapping, redirects to the home endpoint. */
    @GetMapping("/")
    fun redirectToHome(): String = "redirect:/home"

    /** Displays the home page to the user. */
    @GetMapping("/home")
    fun getHomeRoot(@AuthenticationPrincipal user: User, model: Model): String {
        model.addAttribute("username", user.username)
        return "home-root"
    }

    /** Displays the userinfo page to the user. */
    @GetMapping("/home/userinfo")
    fun getUserInfo(@AuthenticationPrincipal user: User, model: Model): String {
        model.addAttribute("userinfo", user.info)
        return "home-userinfo"
    }

    /** Handles user info POST updates received from the userinfo page. */
    @PostMapping("/home/userinfo")
    fun postUserInfo(
        @RequestParam("name") name: String?,
        @RequestParam("email") email: String?,
        @RequestParam("address") address: String?,
        @AuthenticationPrincipal user: User,
        model: Model
    ): String {
        val info = userService.updateUserInfo(user, name, email, address)

        model.addAttribute("userinfo", info)
        model.addAttribute("success", true)
        return "home-userinfo"
    }

    /** Displays every authorized token to the user. */
    @GetMapping("/home/authorizations")
    fun getAuthorizations(@AuthenticationPrincipal principal: User, model: Model): String {
        model.addTokensAuthorizedBy(principal.id)
        return "home-authorizations"
    }

    /**
     * Handles revocation requests received from the authorizations page.
     *
     * The token ID to delete must be sent as a request parameter to the endpoint.
     * Displays the authorizations page with either a success or an error message
     * depending on the result of the operation.
     */
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

        model.addTokensAuthorizedBy(principal.id)
        return "home-authorizations"
    }

    /** Fills the model with every token authorized by the user with the given [userId]. */
    private fun Model.addTokensAuthorizedBy(userId: String) {
        val tokens = tokenRepository.findAllByUserId(userId).map {
            TokenInfo.fromToken(it)
        }

        addAttribute("tokens", tokens)
    }
}
