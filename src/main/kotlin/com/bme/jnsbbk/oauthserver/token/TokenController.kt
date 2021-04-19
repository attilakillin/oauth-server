package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.exceptions.ApiException
import com.bme.jnsbbk.oauthserver.repositories.TransientRepository
import com.bme.jnsbbk.oauthserver.token.validators.TokenValidator
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.time.Instant

@Controller
@RequestMapping("/token")
class TokenController(
    val tokenValidator: TokenValidator,
    val clientRepository: ClientRepository,
    val transientRepository: TransientRepository,
    val tokenRepository: TokenRepository
) {

    @PostMapping
    @ResponseBody
    fun issueToken(@RequestHeader("Authorization") header: String?,
                   @RequestParam params: Map<String, String>): Map<String, String> {
        val client = tokenValidator.validClientOrNull(header, params, clientRepository)
            ?: throw ApiException(HttpStatus.UNAUTHORIZED, "invalid_client")

        return when (params["grant_type"]) {
            "authorization_code" -> handleAuthCode(client, params)
            "refresh_token" -> handleRefreshToken(client, params)
            else -> throw ApiException(HttpStatus.BAD_REQUEST, "unsupported_grant_type")
        }
    }

    private fun handleAuthCode(client: Client, params: Map<String, String>): Map<String, String> {
        val code = transientRepository.findAuthCode(params["code"]!!)
            ?: throw ApiException(HttpStatus.BAD_REQUEST, "invalid_grant")

        if (code.clientId != client.id)
            throw ApiException(HttpStatus.BAD_REQUEST, "invalid_grant")

        // TODO Don't hardcode lifetimes
        val accessToken = Token.accessFromCode(RandomString.generate(), code, 300)
        val refreshToken = Token.refreshFromCode(RandomString.generate(), code, 3600)

        tokenRepository.save(accessToken)
        tokenRepository.save(refreshToken)

        return prepareResult(accessToken, refreshToken)
    }

    private fun handleRefreshToken(client: Client, params: Map<String, String>): Map<String, String> {
        if (params["refresh_token"] == null)
            throw ApiException(HttpStatus.BAD_REQUEST, "invalid_grant")

        val refreshToken = tokenRepository.findRefreshById(params["refresh_token"]!!)
            ?: throw ApiException(HttpStatus.BAD_REQUEST, "invalid_grant")

        if (refreshToken.clientId != client.id) {
            tokenRepository.delete(refreshToken)
            throw ApiException(HttpStatus.BAD_REQUEST, "invalid_grant")
        }

        val accessToken = Token.accessFromRefresh(RandomString.generate(), refreshToken, 300)
        tokenRepository.save(accessToken)

        return prepareResult(accessToken, refreshToken)
    }

    private fun prepareResult(accessToken: Token, refreshToken: Token?): Map<String, String> {
        val result = mutableMapOf(
            "access_token" to accessToken.value,
            "token_type" to "Bearer",
            "expires_in" to Duration.between(Instant.now(), accessToken.expiresAt).seconds.toString(),
            "scope" to accessToken.scope.joinToString(" ")
        )
        if (refreshToken != null)
            result["refresh_token"] = refreshToken.value
        return result
    }
}