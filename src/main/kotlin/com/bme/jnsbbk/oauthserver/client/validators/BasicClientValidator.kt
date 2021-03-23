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

    override fun shouldRejectCreation(client: Client): Boolean {
        if (failsBasicChecks(client))
            return true
        if (client.id != null || client.secret != null)
            return true
        return false
    }

    override fun validateCreationValues(client: Client) {
        fixInconsistentProperties(client)

        client.id = RandomString.generate()
        client.idIssuedAt = Instant.now()

        if (client.tokenEndpointAuthMethod in authMethodsRequiringSecret)
            client.secret = RandomString.generate(48)

        client.registrationAccessToken = RandomString.generate()
    }

    override fun shouldRejectUpdate(old: Client, new: Client): Boolean {
        if (failsBasicChecks(new))
            return true
        if (old.id != new.id || old.secret != new.secret)
            return true
        if (new.expiresAt != null || new.registrationAccessToken != null
            || new.idIssuedAt != null)
                return true
        return false
    }

    override fun validateUpdateValues(old: Client, new: Client) {
        fixInconsistentProperties(new)

        new.idIssuedAt = old.idIssuedAt
        new.expiresAt = old.expiresAt
        new.registrationAccessToken = old.registrationAccessToken
    }

    private fun failsBasicChecks(client: Client): Boolean {
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
        if (client.extraInfo.keys.contains("registration_client_uri"))
            return true
        return false
    }

    private fun fixInconsistentProperties(client: Client) {
        if (client.tokenEndpointAuthMethod.isEmpty())
            client.tokenEndpointAuthMethod = "client_secret_basic"
        if (client.grantTypes.isEmpty())
            client.grantTypes.add("authorization_code")

        // Note: an exception below means that shouldReject() was not used properly
        client.grantTypes.forEach {
            client.responseTypes.add(validGrantPairs[it]!!)
        }
        client.responseTypes.forEach { response ->
            client.grantTypes.add(
                validGrantPairs.filterValues { it == response }.keys.first())
        }
    }
}