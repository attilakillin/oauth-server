package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.jwt.JwtHandler
import com.bme.jnsbbk.oauthserver.repositories.TransientRepository
import com.bme.jnsbbk.oauthserver.token.validators.TokenValidator
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/token")
class TokenController(
    private val tokenValidator: TokenValidator,
    private val clientRepository: ClientRepository,
    private val transientRepository: TransientRepository,
    private val tokenRepository: TokenRepository,
    private val jwtHandler: JwtHandler
) {

    @PostMapping
    @ResponseBody
    fun issueToken(@RequestHeader("Authorization") header: String?,
                   @RequestParam params: Map<String, String>): TokenResponse {
        val client = tokenValidator.validClientOrNull(header, params, clientRepository)
            ?: unauthorized("invalid_client")

        return when (params["grant_type"]) {
            "authorization_code" -> handleAuthCode(client, params["code"])
            "refresh_token" -> handleRefreshToken(client, params["refresh_token"])
            else -> badRequest("unsupported_grant_type")
        }
    }

    private fun handleAuthCode(client: Client, codeValue: String?): TokenResponse {
        if (codeValue == null) badRequest("invalid_grant")

        val code = transientRepository.findAuthCode(codeValue)
            ?: badRequest("invalid_grant")

        transientRepository.removeAuthCode(code)
        if (code.clientId != client.id) badRequest("invalid_grant")

        // TODO Don't hardcode lifetimes
        val accessToken = Token.accessFromCode(RandomString.generate(), code, 300)
        val refreshToken = Token.refreshFromCode(RandomString.generate(), code, 3600)

        tokenRepository.save(accessToken)
        tokenRepository.save(refreshToken)

        return TokenResponse.jwtFromTokens(accessToken, refreshToken, jwtHandler)
    }

    private fun handleRefreshToken(client: Client, refreshValue: String?): TokenResponse {
        if (refreshValue == null) badRequest("invalid_grant")

        val refreshToken = tokenRepository.findRefreshById(refreshValue)
            ?: badRequest("invalid_grant")

        if (refreshToken.clientId != client.id) {
            tokenRepository.delete(refreshToken)
            badRequest("invalid_grant")
        }

        val accessToken = Token.accessFromRefresh(RandomString.generate(), refreshToken, 300)
        tokenRepository.save(accessToken)

        return TokenResponse.jwtFromTokens(accessToken, refreshToken, jwtHandler)
    }
}