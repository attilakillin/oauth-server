package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.AuthCodeRepository
import com.bme.jnsbbk.oauthserver.authorization.entities.isTimestampValid
import com.bme.jnsbbk.oauthserver.client.ClientService
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.exceptions.badRequest
import com.bme.jnsbbk.oauthserver.exceptions.unauthorized
import com.bme.jnsbbk.oauthserver.resource.ResourceServerService
import com.bme.jnsbbk.oauthserver.token.entities.TokenResponse
import com.bme.jnsbbk.oauthserver.token.entities.isTimestampValid
import com.bme.jnsbbk.oauthserver.wellknown.ServerMetadata
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
    private val tokenService: TokenService
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
        @RequestHeader("Authorization") header: String?,
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

    /**
     * Handles requests with the 'authorization_code' grant type.
     *
     * Given an authorization code value, find the related authorization request, and generate
     * a response based on that. The response contains a refresh token and may contain an ID
     * token if the 'openid' scope is present.
     */
    private fun handleAuthCode(client: Client, code: String?): TokenResponse {
        if (code == null) badRequest("invalid_grant")
        val authCode = authCodeRepository.findByIdOrNull(code) ?: badRequest("invalid_grant")

        authCodeRepository.delete(authCode) // The code exists, and was used, so it must be deleted

        if (authCode.clientId != client.id || !authCode.isTimestampValid()) badRequest("invalid_grant")

        return tokenService.createResponseFromAuthCode(authCode)
    }

    /**
     * Handles requests with the 'refresh_token' grant type.
     *
     * Given a refresh token value, find the related refresh token and generate a response based
     * on that. The response will contain a new access token and the received refresh token.
     * No ID token is included, as no user authentication has taken place.
     */
    private fun handleRefreshToken(client: Client, code: String?): TokenResponse {
        if (code == null) badRequest("invalid_grant")
        val refresh = tokenService.findOrRemoveRefreshToken(code, client.id) ?: badRequest("invalid_grant")

        return tokenService.createResponseFromRefreshToken(refresh)
    }

    /**
     * Handles requests with the 'client_credentials' grant type.
     *
     * In this version, the request contains only the scope the client requests (or null, if it requests
     * every scope it has registered with). If the requested scope was declared at registration, we
     * generate an access token and no refresh token (as the client can always request another access token).
     */
    private fun handleClientCredentials(client: Client, scopeIn: String?): TokenResponse {
        var scope = client.scope
        if (scopeIn != null) {
            scope = scopeIn.split(" ").toSet()
            if (scope.any { it !in client.scope }) badRequest("invalid_scope")
        }

        return tokenService.createResponseWithJustAccessToken(client.id, null, scope)
    }

    /**
     * Responds to token introspection requests sent by resource servers.
     *
     * The resource server must authenticate itself, and must send a token to introspect in a JSON object.
     *
     * If the introspection fails for any reason (invalid token or invalid user) the response is simply
     * {"active": "false"}. If it succeeds, a complex JSON response is created.
     */
    @PostMapping(ServerMetadata.Endpoints.tokenIntrospect)
    fun introspectToken(
        @RequestHeader("Authorization") header: String?,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Map<String, String?>> {
        val jwt = body["token"] ?: badRequest("invalid_request_body")
        resourceServerService.authenticateBasic(header) ?: unauthorized("unknown_resource_server")

        val token = tokenService.convertFromJwt(jwt)
        if (token == null || !token.isTimestampValid())
            return ResponseEntity.ok(mapOf("active" to "false"))

        return ResponseEntity.ok(tokenService.createIntrospectResponse(token))
    }

    /**
     * Responds to token revocation requests sent by clients.
     *
     * The client must authenticate itself and must send a token to revoke in its body as a JSON object.
     * The server may respond with a 200 OK even if no token revocation took place.
     */
    @PostMapping(ServerMetadata.Endpoints.tokenRevoke)
    fun revokeToken(
        @RequestHeader("Authorization") header: String?,
        @RequestBody body: Map<String, String>
    ): ResponseEntity<Unit> {
        val token = body["token"] ?: badRequest("invalid_request_body")
        val client = clientService.authenticateWithEither(header, body) ?: unauthorized("invalid_client")

        tokenService.revokeTokenFromString(token, client.id)
        return ResponseEntity.ok().build()
    }
}
