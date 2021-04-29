package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.AuthCodeRepository
import com.bme.jnsbbk.oauthserver.authorization.entities.isTimestampValid
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.token.entities.TokenResponse
import com.bme.jnsbbk.oauthserver.token.entities.isExpired
import com.bme.jnsbbk.oauthserver.token.entities.isTimestampValid
import com.bme.jnsbbk.oauthserver.token.validators.TokenValidator
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/token")
class TokenController(
    private val tokenValidator: TokenValidator,
    private val authCodeRepository: AuthCodeRepository,
    private val tokenRepository: TokenRepository,
    private val tokenFactory: TokenFactory
) {

    @PostMapping
    @ResponseBody
    fun issueToken(@RequestHeader("Authorization") header: String?,
                   @RequestParam params: Map<String, String>): TokenResponse {
        val client = tokenValidator.validClientOrNull(header, params)
            ?: unauthorized("invalid_client")

        return when (params["grant_type"]) {
            "authorization_code" -> handleAuthCode(client, params["code"])
            "refresh_token" -> handleRefreshToken(client, params["refresh_token"])
            else -> badRequest("unsupported_grant_type")
        }
    }

    private fun handleAuthCode(client: Client, codeValue: String?): TokenResponse {
        if (codeValue == null) badRequest("invalid_grant")

        val code = authCodeRepository.findById(codeValue).getOrNull()
            ?: badRequest("invalid_grant")
        authCodeRepository.delete(code)

        if (code.clientId != client.id || !code.isTimestampValid()) badRequest("invalid_grant")

        val accessToken = tokenFactory.accessFromCode(RandomString.generate(), code)
        val refreshToken = tokenFactory.refreshFromCode(RandomString.generate(), code)

        tokenRepository.save(accessToken)
        tokenRepository.save(refreshToken)

        return tokenFactory.responseJWTFromTokens(accessToken, refreshToken)
    }

    private fun handleRefreshToken(client: Client, refreshValue: String?): TokenResponse {
        if (refreshValue == null) badRequest("invalid_grant")

        val refresh = tokenRepository.findRefreshById(refreshValue)
            ?: badRequest("invalid_grant")

        if (refresh.clientId != client.id || refresh.isExpired()) {
            tokenRepository.delete(refresh)
            badRequest("invalid_grant")
        }

        if (!refresh.isTimestampValid()) badRequest("invalid_grant")

        val access = tokenFactory.accessFromRefresh(RandomString.generate(), refresh)
        tokenRepository.save(access)

        return tokenFactory.responseJWTFromTokens(access, refresh)
    }
}