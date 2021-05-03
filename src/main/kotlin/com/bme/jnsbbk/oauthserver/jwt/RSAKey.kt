package com.bme.jnsbbk.oauthserver.jwt

import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

/**
 * Entity class representing a public-private RSA key pair.
 *
 * Each key pair has an associated string [id] used for identification.
 */
@Entity
class RSAKey(
    @Id val id: String,
    @Lob private val encodedPublic: ByteArray,
    @Lob private val encodedPrivate: ByteArray
) {
    val public: PublicKey
        get() = KeyFactory.getInstance("RSA")
            .generatePublic(X509EncodedKeySpec(encodedPublic))

    val private: PrivateKey
        get() = KeyFactory.getInstance("RSA")
            .generatePrivate(PKCS8EncodedKeySpec(encodedPrivate))

    companion object
}

/** Creates a new [RSAKey] with the given [id]. */
fun RSAKey.Companion.newWithId(id: String): RSAKey {
    val keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256)
    return RSAKey(id, keyPair.public.encoded, keyPair.private.encoded)
}

/** Returns the specific algorithm used for creating [RSAKey] instances. */
val RSAKey.Companion.algorithm: String
    get() = SignatureAlgorithm.RS256.value
