package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.AuthCodeRepository
import com.bme.jnsbbk.oauthserver.authorization.entities.isTimestampValid
import com.bme.jnsbbk.oauthserver.client.ClientService
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.jwt.TokenJwtHandler
import com.bme.jnsbbk.oauthserver.resource.ResourceServerService
import com.bme.jnsbbk.oauthserver.token.entities.TokenResponse
import com.bme.jnsbbk.oauthserver.token.entities.isExpired
import com.bme.jnsbbk.oauthserver.token.entities.isTimestampValid
import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.bme.jnsbbk.oauthserver.utils.getOrNull
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/oauth/token")
class TokenController(
    private val clientService: ClientService,
    private val resourceServerService: ResourceServerService,
    private val authCodeRepository: AuthCodeRepository,
    private val tokenRepository: TokenRepository,
    private val tokenFactory: TokenFactory,
    private val tokenJwtHandler: TokenJwtHandler,
    private val userService: UserService
) {

    /**
     * Issues a token to the caller client.
     *
     * The client must validate itself in either the Authorization header or the
     * body using URL encoded form parameters, but not both.
     *
     * Based on the grant_type parameters, different responses are generated.
     */
    @PostMapping
    @ResponseBody
    fun issueToken(
        @RequestHeader("Authorization") header: String?, // The credentials of the client
        @RequestParam params: Map<String, String>
    ): TokenResponse {
        val client = clientService.authenticateWithEither(header, params)
            ?: unauthorized("invalid_client")

        return when (params["grant_type"]) {
            "authorization_code" -> handleAuthCode(client, params["code"])
            "refresh_token" -> handleRefreshToken(client, params["refresh_token"])
            else -> badRequest("unsupported_grant_type")
        }
    }

    /** Handles responses when the grant type was 'authorization_code'. */
    private fun handleAuthCode(client: Client, codeValue: String?): TokenResponse {
        val message = "invalid_grant"
        if (codeValue == null) badRequest(message)

        val code = authCodeRepository.findById(codeValue).getOrNull() ?: badRequest(message)

        authCodeRepository.delete(code) // The code exists, and was used, so it must be deleted

        if (code.clientId != client.id || !code.isTimestampValid()) badRequest(message)

        val accessToken = tokenFactory.accessFromCode(RandomString.generate(), code)
        val refreshToken = tokenFactory.refreshFromCode(RandomString.generate(), code)

        tokenRepository.save(accessToken)
        tokenRepository.save(refreshToken)

        return tokenFactory.responseJwtFromTokens(accessToken, refreshToken)
    }

    /** Handles responses when the grant type was 'refresh_token'. */
    private fun handleRefreshToken(client: Client, refreshValue: String?): TokenResponse {
        val message = "invalid_grant"
        if (refreshValue == null) badRequest(message)

        val refresh = tokenRepository.findRefreshById(refreshValue) ?: badRequest(message)

        if (refresh.clientId != client.id || refresh.isExpired()) {
            tokenRepository.delete(refresh) // If the token is expired or compromised, it must be deleted.
            badRequest(message)
        }

        if (!refresh.isTimestampValid()) badRequest(message)

        val access = tokenFactory.accessFromRefresh(RandomString.generate(), refresh)
        tokenRepository.save(access)

        return tokenFactory.responseJwtFromTokens(access, refresh)
    }

    @PostMapping("/introspect")
    fun introspectToken(
        @RequestHeader("Authorization") header: String?, // The credentials of the resource server
        @RequestParam("token") jwt: String
    ): ResponseEntity<Map<String, String>> {
        resourceServerService.authenticateBasic(header)
            ?: unauthorized("unknown_resource_server")

        val responseOnFail = ResponseEntity.ok(mapOf("active" to "false"))

        val id = tokenJwtHandler.getValidTokenId(jwt) ?: return responseOnFail
        val token = tokenRepository.findAccessById(id) ?: return responseOnFail
        if (!token.isTimestampValid()) return responseOnFail
        val user = userService.getUserById(token.userId) ?: return responseOnFail

        val response = mapOf(
            "active" to "true",
            "iss" to getServerBaseUrl(),
            "sub" to user.id,
            "scope" to token.scope.joinToString(" "),
            "client_id" to token.clientId,
            "username" to user.username
        )

        return ResponseEntity.ok(response)
    }
}
