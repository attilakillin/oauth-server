package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.AuthCodeRepository
import com.bme.jnsbbk.oauthserver.authorization.entities.isTimestampValid
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.token.entities.TokenResponse
import com.bme.jnsbbk.oauthserver.token.entities.isExpired
import com.bme.jnsbbk.oauthserver.token.entities.isTimestampValid
import com.bme.jnsbbk.oauthserver.token.validators.ClientAuthenticator
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/token")
class TokenController(
    private val clientAuthenticator: ClientAuthenticator,
    private val authCodeRepository: AuthCodeRepository,
    private val tokenRepository: TokenRepository,
    private val tokenFactory: TokenFactory
) {

    /**
     * Issues a token to the caller client.
     *
     * The client must validate itself in either the Authorization header or the
     * body using URL encoded form parameters.
     *
     * Based on the grant_type parameters, different responses are generated.
     */
    @PostMapping
    @ResponseBody
    fun issueToken(@RequestHeader("Authorization") header: String?,
                   @RequestParam params: Map<String, String>): TokenResponse {
        val client = clientAuthenticator.validClientOrNull(header, params)
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
        val message = "invalid_grant"

        if (refreshValue == null) badRequest(message)

        val refresh = tokenRepository.findRefreshById(refreshValue)
            ?: badRequest(message)

        if (refresh.clientId != client.id || refresh.isExpired()) {
            tokenRepository.delete(refresh)
            badRequest(message)
        }

        if (!refresh.isTimestampValid()) badRequest(message)

        val access = tokenFactory.accessFromRefresh(RandomString.generate(), refresh)
        tokenRepository.save(access)

        return tokenFactory.responseJWTFromTokens(access, refresh)
    }
}