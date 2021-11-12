package com.bme.jnsbbk.oauthserver.token

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.jwt.AccessTokenHandler
import com.bme.jnsbbk.oauthserver.jwt.IdTokenHandler
import com.bme.jnsbbk.oauthserver.token.entities.*
import com.bme.jnsbbk.oauthserver.user.UserService
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.bme.jnsbbk.oauthserver.utils.getServerBaseUrl
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val accessTokenHandler: AccessTokenHandler,
    private val idTokenHandler: IdTokenHandler,
    private val tokenFactory: TokenFactory,
    private val tokenRepository: TokenRepository,
    private val userService: UserService
) {

    /** Returns whether a token of any type with the given value exists or not. */
    fun tokenExistsByValue(value: String): Boolean {
        return tokenRepository.findByIdOrNull(value) != null
    }

    /** Returns a token with the given value. */
    fun getTokenByValue(value: String): Token? {
        return tokenRepository.findByIdOrNull(value)
    }

    /** Creates a valid token from the given jwt. Returns null if the token is not valid. */
    fun convertFromJwt(jwt: String): Token? {
        return accessTokenHandler.convertToValidToken(jwt)
    }

    /** A shorthand function for creating a unique token ID. */
    private fun generateUniqueId(): String = RandomString.generateUntil { !tokenRepository.existsById(it) }

    /** Creates a relevant token response from an authorization code. */
    fun createResponseFromAuthCode(code: AuthCode): TokenResponse {
        val access = tokenFactory.accessFromCode(generateUniqueId(), code)
        val refresh = tokenFactory.refreshFromCode(generateUniqueId(), code)

        tokenRepository.save(access)
        tokenRepository.save(refresh)

        val response = tokenFactory.responseJwtFromTokens(access, refresh)

        val user = userService.getUserById(code.userId)
        if ("openid" in code.scope && user != null) {
            response.idToken = idTokenHandler.createToken(user, code)
        }

        return response
    }

    /** Creates a relevant token response from a refresh token. */
    fun createResponseFromRefreshToken(refresh: Token): TokenResponse {
        val access = tokenFactory.accessFromRefresh(generateUniqueId(), refresh)
        tokenRepository.save(access)

        return tokenFactory.responseJwtFromTokens(access, refresh)
    }

    /** Finds and in cases deletes the given refresh token value. */
    fun findOrRemoveRefreshToken(value: String, clientId: String): Token? {
        val token = tokenRepository.findByValueAndType(value, TokenType.REFRESH) ?: return null

        if (token.clientId != clientId) {
            tokenRepository.delete(token)
            return null
        }
        return if (token.isTimestampValid()) token else null
    }

    /** Creates a relevant token response with just an access token created here. */
    fun createResponseWithJustAccessToken(clientId: String, scope: Set<String>): TokenResponse {
        val access = tokenFactory.accessFromRawData(generateUniqueId(), clientId, null, scope)
        tokenRepository.save(access)

        return tokenFactory.responseJwtFromTokens(access, null)
    }

    /** Creates an introspection response object from the given token. */
    fun createIntrospectResponse(token: Token): Map<String, String?> {
        val user = userService.getUserById(token.userId)

        return mapOf(
            "active" to "true",
            "iss" to getServerBaseUrl(),
            "sub" to user?.id,
            "scope" to token.scope.joinToString(" "),
            "client_id" to token.clientId,
            "username" to user?.username
        )
    }

    /** Revokes a token with the given value, if, and only if it was issued to the given client. */
    fun revokeTokenFromString(value: String, clientId: String) {
        // The parameter is either an access token JWT or a refresh token string value
        val token = accessTokenHandler.convertToValidToken(value)
            ?: tokenRepository.findByValueAndType(value, TokenType.REFRESH)

        if (token != null && token.clientId == clientId) tokenRepository.delete(token)
    }
}
