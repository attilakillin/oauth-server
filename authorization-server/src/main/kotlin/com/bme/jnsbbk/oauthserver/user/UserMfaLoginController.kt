package com.bme.jnsbbk.oauthserver.user

import dev.samstevens.totp.code.CodeVerifier
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@Controller
@RequestMapping("/user/login/mfa")
class UserMfaLoginController(
    private val mfaVerifier: CodeVerifier,
    private val userRepository: UserRepository
) {

    /** Sends the MFA authentication form for login requests using MFA. */
    @GetMapping
    fun onMfaAuthRequest(@RequestParam target: String, model: Model): String {
        model.addAttribute("target", target)
        return "user-login-mfa-phase"
    }

    @PostMapping
    fun handleMfaAuthentication(
        @RequestParam code: String,
        @RequestParam target: String,
        @AuthenticationPrincipal principal: UserDetails,
        model: Model
    ): String {
        val user = userRepository.findByUsername(principal.username)
        if (user == null) {
            model.addAttribute("errorType", "mfa_auth_user_not_found")
            return "generic-error"
        }

        if (!mfaVerifier.isValidCode(user.mfaSecret, code)) {
            model.addAttribute("target", target)
            model.addAttribute("error", true)
            return "user-login-mfa-phase"
        }

        SecurityContextHolder.getContext().authentication =
            UsernamePasswordAuthenticationToken(user, null, user.authorities)

        val url = Base64.getUrlDecoder().decode(target).toString(Charsets.UTF_8)
        return "redirect:$url"
    }
}
