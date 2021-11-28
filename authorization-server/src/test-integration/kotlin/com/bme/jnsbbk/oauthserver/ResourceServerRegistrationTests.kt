@file:Suppress("FunctionName")
package com.bme.jnsbbk.oauthserver

import com.bme.jnsbbk.oauthserver.components.methods.runResourceServerRegistration
import com.bme.jnsbbk.oauthserver.resource.ResourceServerRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@ActiveProfiles("integration")
@AutoConfigureMockMvc
class ResourceServerRegistrationTests {
    @Autowired private lateinit var mvc: MockMvc
    @Autowired private lateinit var resourceServerRepository: ResourceServerRepository

    @BeforeEach
    fun cleanResourceServerRegistrations() {
        resourceServerRepository.deleteAll()
    }

    @Test
    fun resourceServerRegistration_testRegistration() {
        val scope = setOf("read", "write", "anything")
        val server = runResourceServerRegistration(mvc, scope)

        Assertions.assertTrue(resourceServerRepository.existsById(server.id))
        Assertions.assertEquals(scope, server.scope)
    }
}
