package com.bme.jnsbbk.oauthserver.client.validators

import com.bme.jnsbbk.oauthserver.client.entities.Client
import com.bme.jnsbbk.oauthserver.client.ClientRepository
import com.bme.jnsbbk.oauthserver.client.entities.UnvalidatedClient
import com.bme.jnsbbk.oauthserver.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Instant

/** Reference implementation of [ClientValidator]. Does basic checks on the requested
 *  [UnvalidatedClient], and outputs a valid [Client]. Other implementations may do
 *  additional checks before accepting a registration. */
@Service
class BasicClientValidator : ClientValidator {
    @Autowired private lateinit var clientRepository: ClientRepository

    private object Accepted {
        val authMethods = arrayOf("none", "client_secret_basic", "client_secret_post")
        val authMethodsWithSecret = arrayOf("client_secret_basic", "client_secret_post")
        val grantPairs = mapOf("authorization_code" to "code")
    }

    /** Validates the given [new] client as a new registration.
     *  If the validation fails, [onFailure] is called. */
    override fun validateNewOrElse(new: UnvalidatedClient, onFailure: () -> Nothing): Client {
        if (new.failsBasicChecks() || anyNotNull(new.id, new.secret))
            onFailure()

        val id = RandomString.generateUntil { !clientRepository.existsById(it) }
        val client = Client(id)

        client.fixAndCopyPropertiesFrom(new)
        client.idIssuedAt = Instant.now()
        client.registrationAccessToken = RandomString.generate()

        if (client.tokenEndpointAuthMethod in Accepted.authMethodsWithSecret)
            client.secret = RandomString.generate(48)
        return client
    }

    /** Validates the [new] client as an update request to the [old] client.
     *  If the validation fails, [onFailure] is called. */
    override fun validateUpdateOrElse(new: UnvalidatedClient, old: Client, onFailure: () -> Nothing): Client {
        if (new.failsBasicChecks() || new.id != old.id || new.secret != old.secret)
                onFailure()
        val client = Client(old.id)
        client.fixAndCopyPropertiesFrom(new)

        client.secret = old.secret
        client.idIssuedAt = old.idIssuedAt
        client.expiresAt = old.expiresAt
        client.registrationAccessToken = old.registrationAccessToken
        return client
    }

    /** Returns true if the [UnvalidatedClient] fails any basic checks, and should be rejected. */
    private fun UnvalidatedClient.failsBasicChecks() = anyTrue(
        redirectUris.isNullOrEmpty() || scope.isNullOrEmpty(),
        tokenEndpointAuthMethod != null && tokenEndpointAuthMethod !in Accepted.authMethods,
        grantTypes?.any { it !in Accepted.grantPairs.keys } ?: false,
        responseTypes?.any { it !in Accepted.grantPairs.values } ?: false,
        hasIllegalExtraKeys(),
        hasInvalidChars()
    )

    /** Returns true if the [UnvalidatedClient] contains illegal characters, and should be rejected. */
    private fun UnvalidatedClient.hasInvalidChars(): Boolean {
        return listOf(redirectUris, grantTypes, responseTypes, scope).any { set ->
            set?.any { string -> StringSetConverter.SEPARATOR in string } ?: false
        }
    }

    /** Returns true if the [UnvalidatedClient] has illegal keys in it, and should be rejected. */
    private fun UnvalidatedClient.hasIllegalExtraKeys(): Boolean {
        val keys = listOf("client_id_issued_at", "client_id_expires_at", "registration_client_uri")
        return keys.any { it in extraData.keys }
    }

    /** Copies and fixes otherwise invalid properties from the [request] client to the receiver client. */
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