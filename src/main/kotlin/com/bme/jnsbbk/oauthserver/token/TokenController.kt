package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.exceptions.ApiException
import com.bme.jnsbbk.oauthserver.repositories.TransientRepository
import com.bme.jnsbbk.oauthserver.token.validators.TokenRequestValidator
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/token")
class TokenController(
    val tokenRequestValidator: TokenRequestValidator,
    val clientRepository: ClientRepository,
    val transientRepository: TransientRepository
) {

    @PostMapping
    @ResponseBody
    fun issueToken(@RequestHeader("Authorization") header: String?,
                   @RequestParam params: Map<String, String>): Map<String, String> {
        val validClient = tokenRequestValidator.validateClient(header, params, clientRepository)
        if (validClient.isEmpty)
            throw ApiException(HttpStatus.UNAUTHORIZED, "invalid_client")

        val client = validClient.get()

        // Note: Refactor here for multiple response types
        if (params["grant_type"] != "authorization_code")
            throw ApiException(HttpStatus.BAD_REQUEST, "unsupported_grant_type")

        val authQuery = transientRepository.authCodes[params["code"]]
            ?: throw ApiException(HttpStatus.BAD_REQUEST, "invalid_grant")

        transientRepository.authCodes.remove(params["code"])

        if (authQuery["client_id"] != client.id)
            throw ApiException(HttpStatus.BAD_REQUEST, "invalid_grant")

        val accessToken = RandomString.generate()
        // TODO Store token

        return mapOf("access_token" to accessToken, "token_type" to "Bearer")
    }
}