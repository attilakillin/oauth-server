package com.bme.jnsbbk.oauthserver.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.savedrequest.SavedRequest
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
class MfaLoginSuccessHandler : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(req: HttpServletRequest, res: HttpServletResponse, auth: Authentication) {
        if (SimpleGrantedAuthority("ROLE_PRE_MFA_AUTH") in auth.authorities) {
            res.sendRedirect("/user/login/mfa")
        } else {
            val originalRequest = req.session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") as SavedRequest
            res.sendRedirect(originalRequest.redirectUrl)
        }
    }
}
