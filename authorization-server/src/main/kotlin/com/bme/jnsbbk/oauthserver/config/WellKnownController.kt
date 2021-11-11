package com.bme.jnsbbk.oauthserver.config

import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.jwt.RSAKeyRepository
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.security.interfaces.RSAPublicKey
import java.util.*

@Controller
@RequestMapping("/.well-known")
class WellKnownController(
    private val rsaKeyRepository: RSAKeyRepository
) {

    @GetMapping("/oauth-authorization-server")
    fun getMetadata(): ResponseEntity<ServerMetadata> {
        ServerMetadata.issuer = getServerBaseUrl()
        return ResponseEntity.ok(ServerMetadata)
    }

    @GetMapping("/jwks")
    fun listPublicKeys(): ResponseEntity<Map<String, Any>> {
        val keys = rsaKeyRepository.findAll()
        val jwks = keys.map { RSAKeyResponse(it.id, it.public as RSAPublicKey) }

        return ResponseEntity.ok(mapOf("keys" to jwks))
    }

    @GetMapping("/jwks/{kid}")
    fun getPublicKey(@PathVariable kid: String): ResponseEntity<RSAKeyResponse> {
        val key = rsaKeyRepository.findByIdOrNull(kid) ?: badRequest("invalid_key_id")
        return ResponseEntity.ok(RSAKeyResponse(kid, key.public as RSAPublicKey))
    }

    data class RSAKeyResponse(
        val kid: String,
        val n: String,
        val e: String,
        val alg: String = "RS256",
        val kty: String = "RSA",
        val use: String = "sig"
    ) {
        constructor(kid: String, key: RSAPublicKey) : this(
            kid = kid,
            n = Base64.getUrlEncoder().encodeToString(key.modulus.toByteArray()),
            e = Base64.getUrlEncoder().encodeToString(key.publicExponent.toByteArray())
        )
    }
}
