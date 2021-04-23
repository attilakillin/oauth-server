package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class BasicClientValidator : ClientValidator {
    @Autowired private lateinit var clientRepository: ClientRepository

    private object Whitelist {
        val authMethods = arrayOf("none", "client_secret_basic", "client_secret_post")
        val authMethodsWithSecret = arrayOf("client_secret_basic", "client_secret_post")
        val grantPairs = mapOf("authorization_code" to "code")
    }

    /** Validates the given [client] as a new registration.
     *  If the validation fails, [onFailure] is called. */
    override fun validateNewOr(client: Client, onFailure: () -> Nothing): Client {
        if (client.failsBasicChecks() || anyNotNull(client.id, client.secret))
            onFailure()
        client.fixProperties()

        var id: String
        do { id = RandomString.generate() } while (clientRepository.existsById(id))
        client.id = id
        client.idIssuedAt = Instant.now()
        client.registrationAccessToken = RandomString.generate()

        if (client.tokenEndpointAuthMethod in Whitelist.authMethodsWithSecret)
            client.secret = RandomString.generate(48)
        return client
    }

    /** Validates the [new] client as an update request to the [old] client.
     *  If the validation fails, [onFailure] is called. */
    override fun validateUpdateOr(old: Client, new: Client, onFailure: () -> Nothing): Client {
        if (new.failsBasicChecks() || old.id != new.id || old.secret != new.secret
            || anyNotNull(new.idIssuedAt, new.expiresAt, new.registrationAccessToken))
                onFailure()
        new.fixProperties()

        new.idIssuedAt = old.idIssuedAt
        new.expiresAt = old.expiresAt
        new.registrationAccessToken = old.registrationAccessToken
        return new
    }

    /** Returns true if the [Client] fails any basic checks, and should be rejected. */
    private fun Client.failsBasicChecks() = anyTrue(
        redirectUris.isEmpty() || scope.isEmpty(),
        tokenEndpointAuthMethod.isNotEmpty() && tokenEndpointAuthMethod !in Whitelist.authMethods,
        grantTypes.any { it !in Whitelist.grantPairs.keys },
        responseTypes.any { it !in Whitelist.grantPairs.values },
        "registration_client_uri" in extraInfo.keys,
        hasInvalidChars()
    )

    /** Returns true if the [Client] contain illegal characters, and should be rejected. */
    private fun Client.hasInvalidChars(): Boolean {
        return listOf(redirectUris, grantTypes, responseTypes, scope).any { set ->
            set.any { string -> StringSetConverter.SEPARATOR in string }
        }
    }

    /** Fixes inconsistencies in the [Client] that should not result in rejection. */
    private fun Client.fixProperties() {
        if (tokenEndpointAuthMethod.isEmpty())
            tokenEndpointAuthMethod = "client_secret_basic"
        if (grantTypes.isEmpty()) grantTypes.add("authorization_code")

        grantTypes.forEach { responseTypes.add(Whitelist.grantPairs[it]!!) }
        responseTypes.forEach { grantTypes.add(Whitelist.grantPairs.findKey(it)) }
    }
}