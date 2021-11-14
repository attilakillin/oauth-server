@file:Suppress("unused", "MayBeConstant")
package com.bme.jnsbbk.oauthserver.config

object ServerMetadata {
    object Endpoints {
        const val authorization = "/oauth/authorize"
        const val client = "/oauth/clients"
        const val token = "/oauth/token"
    }

    var issuer: String = ""
    val authorizationEndpoint = Endpoints.authorization
    val tokenEndpoint = Endpoints.token
    val jwksUri = "/.well-known/jwks"
    val registrationEndpoint = Endpoints.client
    val revocationEndpoint = "/oauth/token/revoke"
    val introspectionEndpoint = "/oauth/token/introspect"
    val responseTypesSupported = arrayOf("code", "token")
    val grantTypesSupported = arrayOf("authorization_code", "implicit", "client_credentials")
    val tokenEndpointAuthMethodsSupported = arrayOf("none", "client_secret_basic", "client_secret_post")
    val revocationEndpointAuthMethodsSupported = arrayOf("client_secret_basic")
    val introspectionEndpointAuthMethodsSupported = arrayOf("client_secret_basic")
}
