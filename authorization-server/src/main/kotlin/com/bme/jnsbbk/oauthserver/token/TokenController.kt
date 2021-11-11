package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.AuthCodeRepository
import com.bme.jnsbbk.oauthserver.authorization.entities.isTimestampValid
import com.bme.jnsbbk.oauthserver.client.ClientService
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.config.ServerMetadata
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.jwt.IdTokenJwtHandler
import com.bme.jnsbbk.oauthserver.jwt.TokenJwtHandler
import com.bme.jnsbbk.oauthserver.resource.ResourceServerService
import com.bme.jnsbbk.oauthserver.token.entities.TokenResponse
import com.bme.jnsbbk.oauthserver.token.entities.isExpired
import com.bme.jnsbbk.oauthserver.token.entities.isTimestampValid
import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import io.jsonwebtoken.MalformedJwtException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping(ServerMetadata.Endpoints.token)
class TokenController(
    private val clientService: ClientService,
    private val resourceServerService: ResourceServerService,
    private val authCodeRepository: AuthCodeRepository,
    private val tokenRepository: TokenRepository,
    private val tokenFactory: TokenFactory,
    private val tokenJwtHandler: TokenJwtHandler,
    private val idTokenJwtHandler: IdTokenJwtHandler,
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
            "client_credentials" -> handleClientCredentials(client, params["scope"])
            else -> badRequest("unsupported_grant_type")
        }
    }

    /** Handles responses when the grant type was 'authorization_code'. */
    private fun handleAuthCode(client: Client, codeValue: String?): TokenResponse {
        val message = "invalid_grant"
        if (codeValue == null) badRequest(message)

        val code = authCodeRepository.findByIdOrNull(codeValue) ?: badRequest(message)

        authCodeRepository.delete(code) // The code exists, and was used, so it must be deleted

        if (code.clientId != client.id || !code.isTimestampValid()) badRequest(message)

        val accessToken = tokenFactory.accessFromCode(generateUniqueId(), code)
        val refreshToken = tokenFactory.refreshFromCode(generateUniqueId(), code)

        tokenRepository.save(accessToken)
        tokenRepository.save(refreshToken)

        val response = tokenFactory.responseJwtFromTokens(accessToken, refreshToken)

        val user = userService.getUserById(code.userId)
        if ("openid" in code.scope && user != null) {
            response.idToken = idTokenJwtHandler.createSigned(client.id, user, code.scope, code.nonce)
        }

        return response
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

        val access = tokenFactory.accessFromRefresh(generateUniqueId(), refresh)
        tokenRepository.save(access)

        return tokenFactory.responseJwtFromTokens(access, refresh)
    }

    private fun handleClientCredentials(client: Client, scopeString: String?): TokenResponse {
        val scopeIn = scopeString?.split(" ")?.toSet()

        if (scopeIn != null && scopeIn.any { it !in client.scope }) badRequest("invalid_scope")

        val scope = scopeIn ?: client.scope

        val accessToken = tokenFactory.accessFromRawData(generateUniqueId(), client.id, null, scope)
        tokenRepository.save(accessToken)

        return tokenFactory.responseJwtFromTokens(accessToken, null)
    }

    private fun generateUniqueId(): String = RandomString.generateUntil { !tokenRepository.existsById(it) }

    @PostMapping("/introspect")
    fun introspectToken(
        @RequestHeader("Authorization") header: String?, // The credentials of the resource server
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Map<String, String?>> {
        val jwt = body["token"] ?: badRequest("invalid_request_body")

        resourceServerService.authenticateBasic(header)
            ?: unauthorized("unknown_resource_server")

        val responseOnFail = ResponseEntity.ok(mapOf<String, String?>("active" to "false"))

        val id = try {
            tokenJwtHandler.getValidTokenId(jwt) ?: return responseOnFail
        } catch (ex: MalformedJwtException) {
            badRequest("malformed_token")
        }
        val token = tokenRepository.findAccessById(id) ?: return responseOnFail
        if (!token.isTimestampValid()) return responseOnFail

        var user: User? = null
        if (token.userId != null)
            user = userService.getUserById(token.userId) ?: return responseOnFail

        val response = mapOf(
            "active" to "true",
            "iss" to getServerBaseUrl(),
            "sub" to user?.id,
            "scope" to token.scope.joinToString(" "),
            "client_id" to token.clientId,
            "username" to user?.username
        )

        return ResponseEntity.ok(response)
    }

    @PostMapping("/revoke")
    fun revokeToken(
        @RequestHeader("Authorization") header: String?, // The credentials of the client
        @RequestParam params: Map<String, String>
    ): ResponseEntity<Unit> {
        val client = clientService.authenticateWithEither(header, params)
            ?: unauthorized("invalid_client")

        val tokenIn = params["token"] ?: badRequest("no_token")

        var tokenId = tokenIn
        try { // We try to parse the token as a JWT, but continue silently if we fail
            if (tokenIn.contains('.'))
                tokenId = tokenJwtHandler.getValidTokenId(tokenIn) ?: return ResponseEntity.ok().build()
        } catch (ignored: MalformedJwtException) {}

        val token = tokenRepository.findByIdOrNull(tokenId) ?: return ResponseEntity.ok().build()

        if (token.clientId == client.id) tokenRepository.deleteById(tokenId)

        return ResponseEntity.ok().build()
    }
}
