package com.bme.jnsbbk.oauthserver.config

import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/.well-known")
class WellKnownController {

    @GetMapping("/oauth-authorization-server")
    fun getMetadata(): ResponseEntity<ServerMetadata> {
        ServerMetadata.issuer = getServerBaseUrl()
        return ResponseEntity.ok(ServerMetadata)
    }
}
