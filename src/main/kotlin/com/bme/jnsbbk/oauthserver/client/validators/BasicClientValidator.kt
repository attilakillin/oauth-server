package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.utils.RandomString
import com.bme.jnsbbk.oauthserver.utils.StringSetConverter
import com.bme.jnsbbk.oauthserver.utils.anyNotNull
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class BasicClientValidator : ClientValidator {
    private val validAuthMethods = arrayOf("none", "client_secret_basic", "client_secret_post")
    private val authMethodsRequiringSecret = arrayOf("client_secret_basic", "client_secret_post")
    private val validGrantPairs = mapOf("authorization_code" to "code")

    override fun shouldRejectCreation(client: Client): Boolean {
        return failsBasicChecks(client)
                || hasInvalidChars(client)
                || anyNotNull(client.id, client.secret)
    }

    override fun validateCreationValues(client: Client, repo: ClientRepository) {
        fixInconsistentProperties(client)

        var id: String
        do { id = RandomString.generate() } while (repo.existsById(id))

        client.id = id
        client.idIssuedAt = Instant.now()
        client.registrationAccessToken = RandomString.generate()

        if (client.tokenEndpointAuthMethod in authMethodsRequiringSecret)
            client.secret = RandomString.generate(48)
    }

    override fun shouldRejectUpdate(old: Client, new: Client): Boolean {
        return failsBasicChecks(new)
                || hasInvalidChars(new)
                || old.id != new.id
                || (new.secret != null && new.secret != old.secret)
                || anyNotNull(new.expiresAt, new.registrationAccessToken, new.idIssuedAt)
    }

    override fun validateUpdateValues(old: Client, new: Client) {
        fixInconsistentProperties(new)

        new.idIssuedAt = old.idIssuedAt
        new.expiresAt = old.expiresAt
        new.registrationAccessToken = old.registrationAccessToken
    }

    private fun failsBasicChecks(client: Client): Boolean {
        if (client.redirectUris.isEmpty() || client.scope.isEmpty())
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
        if ("registration_client_uri" in client.extraInfo.keys)
            return true
        return false
    }

    private fun hasInvalidChars(client: Client): Boolean {
        for (set in listOf(client.redirectUris, client.grantTypes, client.responseTypes, client.scope))
            set.forEach { if (StringSetConverter.SEPARATOR in it) return true }
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