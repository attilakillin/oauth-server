@file:Suppress("FunctionName")

package com.bme.jnsbbk.oauthserver

import com.bme.jnsbbk.oauthserver.components.methods.getAndValidateResourceUserToken
import com.bme.jnsbbk.oauthserver.components.methods.runResourceServerRegistration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@ActiveProfiles("integration")
@Sql(scripts = ["classpath:data-integration.sql"])
@WithUserDetails("test")
@AutoConfigureMockMvc
class ResourceUserTokenValidationTests {
    @Autowired private lateinit var mvc: MockMvc

    @Test
    fun resourceUserTokenValidation_testValidation() {
        val server = runResourceServerRegistration(mvc, setOf("read", "write"))

        val username = getAndValidateResourceUserToken(mvc, server)

        Assertions.assertEquals("test", username)
    }
}
