package com.bme.jnsbbk.oauthserver.wellknown

import java.security.interfaces.RSAPublicKey
import java.util.*

/** A data class conforming to the JWKS standard structure of an RSA public key. */
data class RSAWebKey(
    val kid: String,
    val n: String,
    val e: String,
    val alg: String = "RS256",
    val kty: String = "RSA",
    val use: String = "sig"
) {
    /** Constructs a web key using a Java RSA public key. */
    constructor(kid: String, key: RSAPublicKey) : this(
        kid = kid,
        n = Base64.getUrlEncoder().encodeToString(key.modulus.toByteArray()),
        e = Base64.getUrlEncoder().encodeToString(key.publicExponent.toByteArray())
    )
}
