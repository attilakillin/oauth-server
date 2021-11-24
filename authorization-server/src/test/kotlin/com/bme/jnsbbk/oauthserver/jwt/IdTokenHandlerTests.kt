package com.bme.jnsbbk.oauthserver.jwt

import com.bme.jnsbbk.oauthserver.authorization.entities.AuthCode
import com.bme.jnsbbk.oauthserver.config.AppConfig
import com.bme.jnsbbk.oauthserver.user.entities.User
import com.bme.jnsbbk.oauthserver.user.entities.UserInfo
import com.bme.jnsbbk.oauthserver.utils.getIssuerString
import io.jsonwebtoken.Jwts
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant

@ExtendWith(MockKExtension::class)
class IdTokenHandlerTests {
    private val rsaKeyRepository = mockk<RSAKeyRepository>()
    private val appConfig = AppConfig(
        tokens = AppConfig.Tokens(
            idToken = AppConfig.Lifespan(0, 60)
        )
    )

    private val keys = RSAKey.newWithId("id")
    init {
        mockkStatic(::getIssuerString)
        every { getIssuerString() } returns "issuer"
        every { rsaKeyRepository.findByIdOrNull(any()) } returns keys
    }

    private val handler = IdTokenHandler(appConfig, rsaKeyRepository)

    val user = User(
        id = "user_id",
        username = "username",
        password = "password"
    ).apply { info = UserInfo("Name", "Email", "Address") }
    val code = AuthCode(
        value = "code_value",
        clientId = "client_id",
        userId = user.id,
        scope = setOf("openid"),
        nonce = null,
        issuedAt = Instant.now(),
        notBefore = Instant.now(),
        expiresAt = null
    )

    /** Test function: createToken() */

    @Test
    fun createToken_withNoExtraScope() {
        val jwt = handler.createToken(user, code)

        val parser = Jwts.parserBuilder().setSigningKey(keys.public).build()
        val claims = parser.parseClaimsJws(jwt)

        Assertions.assertEquals("issuer", claims.body.issuer)
        Assertions.assertEquals(user.id, claims.body.subject)
        Assertions.assertEquals(code.clientId, claims.body.audience)
        Assertions.assertNull(claims.body["nonce"])
        Assertions.assertNull(claims.body["name"])
        Assertions.assertNull(claims.body["email"])
        Assertions.assertNull(claims.body["address"])
    }

    @Test
    fun createToken_withAllExtraScope() {
        val code = code.copy(scope = setOf("openid", "profile", "email", "address"))

        val jwt = handler.createToken(user, code)

        val parser = Jwts.parserBuilder().setSigningKey(keys.public).build()
        val claims = parser.parseClaimsJws(jwt)

        Assertions.assertEquals("issuer", claims.body.issuer)
        Assertions.assertEquals(user.id, claims.body.subject)
        Assertions.assertEquals(code.clientId, claims.body.audience)
        Assertions.assertNull(claims.body["nonce"])
        Assertions.assertEquals(user.info.name, claims.body["name"])
        Assertions.assertEquals(user.info.email, claims.body["email"])
        Assertions.assertEquals(user.info.address, claims.body["address"])
    }

    @Test
    fun createToken_withNonce() {
        val code = code.copy(nonce = "nonce_value")

        val jwt = handler.createToken(user, code)

        val parser = Jwts.parserBuilder().setSigningKey(keys.public).build()
        val claims = parser.parseClaimsJws(jwt)

        Assertions.assertEquals("nonce_value", claims.body["nonce"])
    }
}
