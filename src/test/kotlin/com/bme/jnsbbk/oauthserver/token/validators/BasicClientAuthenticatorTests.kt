package com.bme.jnsbbk.oauthserver.token.validators

import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.*

@ExtendWith(MockitoExtension::class)
class BasicClientAuthenticatorTests {
    @Mock private lateinit var repository: ClientRepository
    @InjectMocks private val authenticator = BasicClientAuthenticator()

    companion object {
        @BeforeAll
        fun initialize() {
            MockitoAnnotations.initMocks(this)
        }
    }

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
        whenever(repository.findById(id)).thenReturn(Optional.of(client))
        val header = "Basic " + encodeString("$id:$secret")
        assert(authenticator.validClientOrNull(header, mapOf()) == client)
    }

    @Test
    fun validClientOrNull_nullWhenNoIdInHeader() {
        whenever(repository.findById(any())).thenReturn(Optional.empty())
        val header = "Basic " + encodeString(":$secret")
        assert(authenticator.validClientOrNull(header, mapOf()) == null)
    }

    @Test
    fun validClientOrNull_nullWhenNoSecretInHeader() {
        whenever(repository.findById(id)).thenReturn(Optional.of(client))
        val header = "Basic " + encodeString(id)
        assert(authenticator.validClientOrNull(header, mapOf()) == null)
    }

    @Test
    fun validClientOrNull_acceptsBody() {
        whenever(repository.findById(id)).thenReturn(Optional.of(client))
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
        whenever(repository.findById(id)).thenReturn(Optional.of(client))
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
