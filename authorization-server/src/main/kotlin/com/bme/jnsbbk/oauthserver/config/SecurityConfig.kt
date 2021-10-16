package com.bme.jnsbbk.oauthserver.config

import com.bme.jnsbbk.oauthserver.user.UserService
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val passwordEncoder: PasswordEncoder,
    private val userService: UserService
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        val excluded = arrayOf(
            "/oauth/clients",
            "/oauth/resource",
            "/oauth/resource/user/validate",
            "/oauth/token",
            "/oauth/token/**",
            "/user/register"
        )

        http
            .authorizeRequests()
                .antMatchers("/css/**", "/js/**").permitAll()
                .antMatchers(*excluded).permitAll()
                .anyRequest().authenticated()
                .and()
            .csrf()
                .ignoringAntMatchers(*excluded)
                .and()
            .formLogin()
                .loginPage("/user/login")
                .loginProcessingUrl("/user/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
                .and()
            .logout()
                .permitAll()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth
            .userDetailsService(userService)
            .passwordEncoder(passwordEncoder)
    }
}
