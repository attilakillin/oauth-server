package com.bme.jnsbbk.oauthserver.client

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.validators.ClientValidator
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.*

@WebMvcTest(ClientRegistrationController::class)
class ClientRegistrationControllerTests {
    @Autowired private lateinit var mockMvc: MockMvc

    @MockkBean private lateinit var clientRepository: ClientRepository
    @MockkBean private lateinit var clientValidator: ClientValidator

    private val client = Client(RandomString.generate())

    init {
        client.secret = null
        client.redirectUris = setOf()
        client.grantTypes = setOf()
        client.responseTypes = setOf()
        client.scope = setOf()
        client.idIssuedAt = Instant.now()
        client.expiresAt = Instant.now().plusSeconds(60)
        client.tokenEndpointAuthMethod = ""
        client.registrationAccessToken = RandomString.generate()
    }

    @Test
    fun registerClient_400OnValidationFail() {
        every { clientValidator.validateNewOrElse(any(), any()) } answers {
            secondArg<() -> Nothing>().invoke()
        }

        mockMvc
            .perform(post("/register"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun registerClient_200OnValidationSuccess() {
        every { clientValidator.validateNewOrElse(any(), any()) } returns client
        every { clientRepository.save(any()) } answers { firstArg() }

        mockMvc
            .perform(
                post("/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"client_name": "Test client"}""")
            )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(client.id)))
    }

    @Test
    fun getUpdateDelete_allReturn401OnAccessTokenMismatch() {
        every { clientRepository.findById(client.id) } returns Optional.of(client)
        val invalidValue = "Bearer invalid_access_token"

        mockMvc
            .perform(
                get("/register/${client.id}")
                    .header("Authorization", invalidValue)
            )
            .andExpect(status().isUnauthorized)

        mockMvc
            .perform(
                put("/register/${client.id}")
                    .header("Authorization", invalidValue)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"client_name": "Test client"}""")
            )
            .andExpect(status().isUnauthorized)

        mockMvc
            .perform(
                delete("/register/${client.id}")
                    .header("Authorization", invalidValue)
            )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun getUpdateDelete_allReturn401OnInvalidClientId() {
        val copy = Client(client.id)
        copy.registrationAccessToken = "something different"
        every { clientRepository.findById(client.id) } returns Optional.of(copy)

        mockMvc
            .perform(get("/register/clientIdThatIsInvalid"))
            .andExpect(status().isUnauthorized)

        mockMvc
            .perform(
                put("/register/clientIdThatIsInvalid")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"client_name": "Test client"}""")
            )
            .andExpect(status().isUnauthorized)

        mockMvc
            .perform(delete("/register/clientIdThatIsInvalid"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun getUpdateDelete_allReturn401OnNoAuthHeader() {
        mockMvc
            .perform(get("/register/${client.id}"))
            .andExpect(status().isUnauthorized)

        mockMvc
            .perform(
                put("/register/${client.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"client_name": "Test client"}""")
            )
            .andExpect(status().isUnauthorized)

        mockMvc
            .perform(delete("/register/${client.id}"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun getClient_200OnSuccess() {
        every { clientRepository.findById(client.id) } returns Optional.of(client)

        mockMvc
            .perform(
                get("/register/${client.id}")
                    .header("Authorization", "Bearer ${client.registrationAccessToken}")
            )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(client.id)))
    }

    @Test
    fun deleteClient_204OnSuccess() {
        every { clientRepository.findById(client.id) } returns Optional.of(client)
        every { clientRepository.delete(any()) } returns Unit

        mockMvc
            .perform(
                delete("/register/${client.id}")
                    .header("Authorization", "Bearer ${client.registrationAccessToken}")
            )
            .andExpect(status().isNoContent)

        verify(exactly = 1) { clientRepository.delete(any()) }
    }

    @Test
    fun updateClient_400OnValidationFail() {
        every { clientRepository.findById(client.id) } returns Optional.of(client)
        every { clientValidator.validateUpdateOrElse(any(), any(), any()) } answers {
            thirdArg<() -> Nothing>().invoke()
        }

        mockMvc
            .perform(
                put("/register/${client.id}")
                    .header("Authorization", "Bearer ${client.registrationAccessToken}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"client_name": "Test client"}""")
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun updateClient_200OnSuccess() {
        every { clientRepository.findById(client.id) } returns Optional.of(client)
        every { clientRepository.save(client) } returns client
        every { clientValidator.validateUpdateOrElse(any(), any(), any()) } returns client

        mockMvc
            .perform(
                put("/register/${client.id}")
                    .header("Authorization", "Bearer ${client.registrationAccessToken}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"client_name": "Test client"}""")
            )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(client.id)))
    }
}
