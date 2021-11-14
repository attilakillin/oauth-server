package com.bme.jnsbbk.oauthserver.wellknown

import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.jwt.RSAKeyRepository
import com.bme.jnsbbk.oauthserver.utils.getIssuerString
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.security.interfaces.RSAPublicKey

@Controller
@RequestMapping("/.well-known")
class WellKnownController(
    private val rsaKeyRepository: RSAKeyRepository
) {

    /** Returns the metadata object containing information about the server. */
    @GetMapping("/oauth-authorization-server")
    fun getMetadata(): ResponseEntity<ServerMetadata> {
        ServerMetadata.issuer = getIssuerString()
        return ResponseEntity.ok(ServerMetadata)
    }

    /** Returns the JSON Web Key Set as specified by the standard. */
    @GetMapping("/jwks")
    fun listPublicKeys(): ResponseEntity<Map<String, Any>> {
        val keys = rsaKeyRepository.findAll().map { RSAWebKey(it.id, it.public as RSAPublicKey) }

        return ResponseEntity.ok(mapOf("keys" to keys))
    }

    /** Returns one specific JSON Web Key in the structure specified by the standard. */
    @GetMapping("/jwks/{kid}")
    fun getPublicKey(@PathVariable kid: String): ResponseEntity<RSAWebKey> {
        val key = rsaKeyRepository.findByIdOrNull(kid) ?: badRequest("invalid_key_id")
        return ResponseEntity.ok(RSAWebKey(key.id, key.public as RSAPublicKey))
    }
}
