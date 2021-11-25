package com.bme.jnsbbk.oauthserver.wellknown

import com.bme.jnsbbk.oauthserver.jwt.RSAKey
import com.bme.jnsbbk.oauthserver.jwt.RSAKeyRepository
import com.bme.jnsbbk.oauthserver.jwt.newWithId
import com.bme.jnsbbk.oauthserver.utils.getIssuerString
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockkStatic
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    WellKnownController::class,
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [WebSecurityConfigurer::class])],
    excludeAutoConfiguration = [SecurityAutoConfiguration::class]
)
class WellKnownControllerTests {
    @Autowired private lateinit var mvc: MockMvc
    @MockkBean private lateinit var rsaKeyRepository: RSAKeyRepository

    private val keys = RSAKey.newWithId("unique_key_id")

    @Test
    fun getPublicKey_withValidKeyId() {
        every { rsaKeyRepository.findByIdOrNull(keys.id) } returns keys

        mvc
            .perform(get("/.well-known/jwks/${keys.id}"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(keys.id)))
    }

    @Test
    fun getPublicKey_withInvalidKeyId() {
        every { rsaKeyRepository.findByIdOrNull("invalid") } returns null

        mvc
            .perform(get("/.well-known/jwks/invalid"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun listPublicKeys_withValidKey() {
        every { rsaKeyRepository.findAll() } returns listOf(keys)

        mvc
            .perform(get("/.well-known/jwks"))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(keys.id)))
    }

    @Test
    fun getMetadata_worksAsExpected() {
        mockkStatic(::getIssuerString)
        every { getIssuerString() } returns "issuer"

        mvc
            .perform(get("/.well-known/oauth-authorization-server"))
            .andExpect(status().isOk)
    }
}
