package com.bme.jnsbbk.oauthserver.home

import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.user.entities.UserInfo
import com.bme.jnsbbk.oauthserver.user.entities.fromNullable
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class HomeController(
    private val userService: UserService
) {

    @GetMapping("/")
    fun redirectToHome(): String = "redirect:/home"

    @GetMapping("/home")
    fun getHomeRoot(
        @AuthenticationPrincipal principal: User,
        model: Model
    ): String {
        model.addAttribute("username", principal.username)
        return "home-root"
    }

    @GetMapping("/home/userinfo")
    fun getUserInfo(
        @AuthenticationPrincipal principal: User,
        model: Model
    ): String {
        model.addAttribute("userinfo", principal.info)
        return "home-userinfo"
    }

    @PostMapping("/home/userinfo")
    fun postUserInfo(
        @RequestParam("name") name: String?,
        @RequestParam("email") email: String?,
        @RequestParam("address") address: String?,
        @AuthenticationPrincipal principal: User,
        model: Model
    ): String {
        principal.info = UserInfo.fromNullable(name, email, address)
        userService.updateUser(principal)

        model.addAttribute("userinfo", principal.info)
        model.addAttribute("success", true)
        return "home-userinfo"
    }
}
