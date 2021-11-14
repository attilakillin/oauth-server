@file:Suppress("unused", "MayBeConstant")
package com.bme.jnsbbk.oauthserver.wellknown

/** A metadata object containing endpoint information regarding the authorization server. */
object ServerMetadata {
    /** Contains constant fields, used for controller class parameters. */
    object Endpoints {
        const val authorization = "/oauth/authorize"
        const val client = "/oauth/clients"
        const val token = "/oauth/token"
        const val tokenRevoke = "/revoke"
        const val tokenIntrospect = "/introspect"
        const val jsonWebKeySet = "/.well-known/jwks"
    }

    var issuer: String = ""

    val authorizationEndpoint = Endpoints.authorization
    val registrationEndpoint = Endpoints.client
    val tokenEndpoint = Endpoints.token
    val revocationEndpoint = Endpoints.token + Endpoints.tokenRevoke
    val introspectionEndpoint = Endpoints.token + Endpoints.tokenIntrospect
    val jwksUri = Endpoints.jsonWebKeySet

    val responseTypesSupported = arrayOf("code", "token")
    val grantTypesSupported = arrayOf("authorization_code", "implicit", "client_credentials")
    val tokenEndpointAuthMethodsSupported = arrayOf("none", "client_secret_basic", "client_secret_post")
    val revocationEndpointAuthMethodsSupported = arrayOf("client_secret_basic")
    val introspectionEndpointAuthMethodsSupported = arrayOf("client_secret_basic")
}
