package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.utils.RandomString
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class BasicClientValidator : ClientValidator {
    private val validAuthMethods = arrayOf("none", "client_secret_basic", "client_secret_post")
    private val authMethodsRequiringSecret = arrayOf("client_secret_basic", "client_secret_post")
    private val validGrantPairs = mapOf("authorization_code" to "code")

    override fun shouldReject(client: Client): Boolean {
        if (client.id != null || client.secret != null)
            return true
        if (client.redirectUris.isEmpty())
            return true
        if (client.tokenEndpointAuthMethod.isNotEmpty() &&
            client.tokenEndpointAuthMethod !in validAuthMethods)
            return true
        client.grantTypes.forEach {
            if (it !in validGrantPairs.keys) return true
        }
        client.responseTypes.forEach {
            if (it !in validGrantPairs.values) return true
        }
        return false
    }

    override fun parseAndValidate(client: Client) {
        client.tokenEndpointAuthMethod.ifEmpty { "secret_basic" }
        client.grantTypes.ifEmpty { "authorization_code" }

        // Note: an exception below means that shouldReject() was not used properly
        client.grantTypes.forEach {
            client.responseTypes.add(validGrantPairs[it]!!)
        }
        client.responseTypes.forEach { response ->
            client.grantTypes.add(
                validGrantPairs.filterValues { it == response }.keys.first())
        }


        client.id = RandomString.generate()
        client.registrationTime = Instant.now()

        if (client.tokenEndpointAuthMethod in authMethodsRequiringSecret)
            client.secret = RandomString.generate(48)

        client.registrationAccessToken = RandomString.generate()
    }
}