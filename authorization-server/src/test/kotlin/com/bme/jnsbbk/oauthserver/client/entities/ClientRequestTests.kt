package com.bme.jnsbbk.oauthserver.client.entities

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ClientRequestTests {
    @Test
    fun putExtraData_worksAsExpected() {
        val request = ClientRequest(
            id = null,
            secret = null,
            redirectUris = null,
            tokenEndpointAuthMethod = null,
            grantTypes = null,
            responseTypes = null,
            scope = null
        )
        request.putExtraData("key", "value")
        Assertions.assertEquals("value", request.extraData["key"])
    }
}
