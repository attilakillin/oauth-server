package com.bme.jnsbbk.oauthserver.jwt

import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.security.PrivateKey
import java.security.PublicKey
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob

@Entity
data class RSAKey (
    @Id
    val id: String,
    @Lob @Column(columnDefinition = "CLOB")
    val public: PublicKey,
    @Lob @Column(columnDefinition = "CLOB")
    val private: PrivateKey
) { companion object }

fun RSAKey.Companion.newWithId(id: String): RSAKey {
    val keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256)
    return RSAKey(id, keyPair.public, keyPair.private)
}

val RSAKey.Companion.algorithm: String
    get() = SignatureAlgorithm.RS256.value