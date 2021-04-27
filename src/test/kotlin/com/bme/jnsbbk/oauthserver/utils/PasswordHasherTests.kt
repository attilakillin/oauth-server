package com.bme.jnsbbk.oauthserver.utils

import org.junit.jupiter.api.Test

class PasswordHasherTests {
    @Test
    fun hash_saltingDiffersForRepeatedRequests() {
        val password = RandomString.generate()
        val hashes = MutableList(10) { PasswordHasher.hash(password) }
        val distinct = hashes.distinct().size
        val original = hashes.size
        assert(distinct == original) {
            "Out of $original hashes of the same password, only $distinct were distinct!"
        }
    }

    @Test
    fun matchesHash_returnsTrueWithSamePassword() {
        val password = RandomString.generate()
        val hash = PasswordHasher.hash(password)
        assert(PasswordHasher.matchesHash(password, hash))
    }

    @Test
    fun matchesHash_returnsFalseWithDifferentPassword() {
        val size = 10
        val passwords = mutableListOf<String>()
        for (i in 0 until size)
            passwords.add(RandomString.generateUntil { it !in passwords })
        val hashes = MutableList(10) { PasswordHasher.hash(passwords[it]) }

        for (i in 1 until size) {
            assert(!PasswordHasher.matchesHash(passwords[i], hashes[i - 1]))
        }
    }
}