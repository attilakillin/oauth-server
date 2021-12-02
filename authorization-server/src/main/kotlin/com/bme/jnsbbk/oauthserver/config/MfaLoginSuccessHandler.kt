package com.bme.jnsbbk.oauthserver.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.SavedRequest
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
class MfaLoginSuccessHandler : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(req: HttpServletRequest, res: HttpServletResponse, auth: Authentication) {
        val originalRequest = req.session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") as SavedRequest

        if (SimpleGrantedAuthority("ROLE_PRE_MFA_AUTH") in auth.authorities) {
            val target = Base64.getUrlEncoder().encodeToString(originalRequest.redirectUrl.toByteArray())
            res.sendRedirect("/user/login/mfa?target=$target")
        } else {
            res.sendRedirect(originalRequest.redirectUrl)
        }
    }
}
