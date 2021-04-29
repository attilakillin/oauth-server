package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.UnvalidatedClient
import com.bme.jnsbbk.oauthserver.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * Reference implementation of [ClientValidator].
 *
 * Does basic checks on the requested [UnvalidatedClient], and outputs a valid [Client].
 * Other implementations may do additional checks before accepting a registration.
 */
@Service
class BasicClientValidator : ClientValidator {
    @Autowired private lateinit var clientRepository: ClientRepository

    private object Accepted {
        val authMethods = arrayOf("none", "client_secret_basic", "client_secret_post")
        val authMethodsWithSecret = arrayOf("client_secret_basic", "client_secret_post")
        val grantPairs = mapOf("authorization_code" to "code")
    }

    override fun validateNewOrElse(new: UnvalidatedClient, onFailure: () -> Nothing): Client {
        if (new.failsBasicChecks() || anyNotNull(new.id, new.secret)) {
            onFailure()
        }

        val id = RandomString.generateUntil { !clientRepository.existsById(it) }
        val client = Client(id)

        client.fixAndCopyPropertiesFrom(new)
        client.idIssuedAt = Instant.now()
        client.registrationAccessToken = RandomString.generate()

        if (client.tokenEndpointAuthMethod in Accepted.authMethodsWithSecret)
            client.secret = RandomString.generate(48)
        return client
    }

    override fun validateUpdateOrElse(new: UnvalidatedClient, old: Client, onFailure: () -> Nothing): Client {
        if (new.failsBasicChecks() || new.id != old.id || new.secret != old.secret) {
            onFailure()
        }

        val client = Client(old.id)
        client.fixAndCopyPropertiesFrom(new)

        client.secret = old.secret
        client.idIssuedAt = old.idIssuedAt
        client.expiresAt = old.expiresAt
        client.registrationAccessToken = old.registrationAccessToken
        return client
    }

    private fun UnvalidatedClient.failsBasicChecks() = anyTrue(
        redirectUris.isNullOrEmpty(),
        scope.isNullOrEmpty(),
        tokenEndpointAuthMethod != null && tokenEndpointAuthMethod !in Accepted.authMethods,
        grantTypes?.any { it !in Accepted.grantPairs.keys } ?: false,
        responseTypes?.any { it !in Accepted.grantPairs.values } ?: false,
        hasIllegalExtraKeys(),
        hasInvalidChars()
    )

    private fun UnvalidatedClient.hasInvalidChars(): Boolean {
        return listOf(redirectUris, grantTypes, responseTypes, scope).any { set ->
            set?.any { string -> StringSetConverter.SEPARATOR in string } ?: false
        }
    }

    private fun UnvalidatedClient.hasIllegalExtraKeys(): Boolean {
        val keys = listOf("client_id_issued_at", "client_id_expires_at", "registration_client_uri")
        return keys.any { it in extraData.keys }
    }

    private fun Client.fixAndCopyPropertiesFrom(request: UnvalidatedClient) {
        redirectUris = request.redirectUris!!
        scope = request.scope!!
        tokenEndpointAuthMethod = request.tokenEndpointAuthMethod ?: "client_secret_basic"

        val grants = request.grantTypes?.toMutableSet() ?: mutableSetOf("authorization_code")
        val responses = request.responseTypes?.toMutableSet() ?: mutableSetOf()
        grants.forEach { responses.add(Accepted.grantPairs[it]!!) }
        responses.forEach { grants.add(Accepted.grantPairs.findKey(it)) }

        grantTypes = grants
        responseTypes = responses
        extraData.putAll(request.extraData)
    }
}