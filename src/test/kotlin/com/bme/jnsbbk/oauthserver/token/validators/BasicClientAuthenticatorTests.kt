package com.bme.jnsbbk.oauthserver.token.validators

import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.utils.RandomString
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class BasicClientAuthenticatorTests {
    @MockK private lateinit var repository: ClientRepository
    @InjectMockKs private var authenticator = BasicClientAuthenticator()

    private lateinit var id: String
    private lateinit var secret: String
    private lateinit var client: Client

    private fun encodeString(what: String) =
        Base64.getUrlEncoder().encode(what.toByteArray()).toString(Charsets.UTF_8)

    @BeforeEach
    fun createClient() {
        id = RandomString.generate()
        secret = RandomString.generate()
        client = Client(id)
        client.secret = secret
    }

    @Test
    fun validClientOrNull_nullWhenNoAuthentication() {
        assert(authenticator.validClientOrNull(null, mapOf()) == null)
    }

    @Test
    fun validClientOrNull_acceptsHeader() {
        every { repository.findById(id) } returns Optional.of(client)
        val header = "Basic " + encodeString("$id:$secret")
        assert(authenticator.validClientOrNull(header, mapOf()) == client)
    }

    @Test
    fun validClientOrNull_nullWhenNoIdInHeader() {
        every { repository.findById(any()) } returns Optional.empty()
        val header = "Basic " + encodeString(":$secret")
        assert(authenticator.validClientOrNull(header, mapOf()) == null)
    }

    @Test
    fun validClientOrNull_nullWhenNoSecretInHeader() {
        every { repository.findById(id) } returns Optional.of(client)
        val header = "Basic " + encodeString(id)
        assert(authenticator.validClientOrNull(header, mapOf()) == null)
    }

    @Test
    fun validClientOrNull_acceptsBody() {
        every { repository.findById(id) } returns Optional.of(client)
        val body = mapOf("client_id" to id, "client_secret" to secret)
        assert(authenticator.validClientOrNull(null, body) == client)
    }

    @Test
    fun validClientOrNull_nullWhenNoIdInBody() {
        val body = mapOf("client_secret" to secret)
        assert(authenticator.validClientOrNull(null, body) == null)
    }

    @Test
    fun validClientOrNull_nullWhenNoSecretInBody() {
        every { repository.findById(id) } returns Optional.of(client)
        val body = mapOf("client_id" to id)
        assert(authenticator.validClientOrNull(null, body) == null)
    }

    @Test
    fun validClientOrNull_nullWhenCredentialsInBoth() {
        val header = "Basic " + encodeString("$id:$secret")
        val body = mapOf("client_id" to id, "client_secret" to secret)
        assert(authenticator.validClientOrNull(header, body) == null)
    }
}
