package com.bme.jnsbbk.oauthserver.users

import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.utils.PasswordHasher
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.MacProvider
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.security.Key
import java.time.Instant
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Controller
@RequestMapping("/user/login")
class UserLoginController (
    private val userRepository: UserRepository
) {
    private val tokenSigningKey: Key
    init {
        val key = MacProvider.generateKey(SignatureAlgorithm.HS256).encoded
        tokenSigningKey = SecretKeySpec(key, SignatureAlgorithm.HS256.jcaName)
    }

    @GetMapping
    fun serveRequest(): String = "user_login_form"

    @PostMapping
    @ResponseBody
    fun handleLogin(@RequestParam email: String,
                    @RequestParam password: String): ResponseEntity<String> {
        val user = userRepository.findByEmail(email) ?: badRequest("Invalid credentials!")
        if (!PasswordHasher.matchesHash(password, user.passwordHash)) {
            badRequest("Invalid credentials!")
        }

        val token = Jwts.builder()
            .setHeader(mapOf("typ" to "JWT", "alg" to tokenSigningKey.algorithm))
            .setIssuer(getServerBaseUrl())
            .setSubject(user.id)
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(Instant.now().plusSeconds(300)))
            .signWith(tokenSigningKey)
            .compact()

        return ResponseEntity.ok(token)
    }
}