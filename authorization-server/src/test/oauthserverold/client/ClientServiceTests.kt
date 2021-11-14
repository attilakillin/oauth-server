package com.bme.jnsbbk.oauthserverold.client

import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.ClientService
import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.utils.RandomString
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class ClientServiceTests {
    @MockK private lateinit var repository: ClientRepository
    @InjectMockKs private lateinit var service: ClientService

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
        assertNull(service.authenticateWithEither(null, mapOf()))
    }

    @Test
    fun validClientOrNull_acceptsHeader() {
        every { repository.findById(id) } returns Optional.of(client)

        val header = "Basic " + encodeString("$id:$secret")
        Assertions.assertEquals(client, service.authenticateWithEither(header, mapOf()))
    }

    @Test
    fun validClientOrNull_nullWhenNoIdInHeader() {
        every { repository.findById(any()) } returns Optional.empty()

        val header = "Basic " + encodeString(":$secret")
        assertNull(service.authenticateWithEither(header, mapOf()))
    }

    @Test
    fun validClientOrNull_nullWhenNoSecretInHeader() {
        every { repository.findById(id) } returns Optional.of(client)

        val header = "Basic " + encodeString(id)
        assertNull(service.authenticateWithEither(header, mapOf()))
    }

    @Test
    fun validClientOrNull_acceptsBody() {
        every { repository.findById(id) } returns Optional.of(client)

        val body = mapOf("client_id" to id, "client_secret" to secret)
        Assertions.assertEquals(client, service.authenticateWithEither(null, body))
    }

    @Test
    fun validClientOrNull_nullWhenNoIdInBody() {
        val body = mapOf("client_secret" to secret)
        assertNull(service.authenticateWithEither(null, body))
    }

    @Test
    fun validClientOrNull_nullWhenNoSecretInBody() {
        every { repository.findById(id) } returns Optional.of(client)

        val body = mapOf("client_id" to id)
        assertNull(service.authenticateWithEither(null, body))
    }

    @Test
    fun validClientOrNull_nullWhenCredentialsInBoth() {
        every { repository.findById(id) } returns Optional.of(client)

        val header = "Basic " + encodeString("$id:$secret")
        val body = mapOf("client_id" to id, "client_secret" to secret)
        assertNull(service.authenticateWithEither(header, body))
    }
}
