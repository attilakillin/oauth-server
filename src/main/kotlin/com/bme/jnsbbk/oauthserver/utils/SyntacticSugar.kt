package com.bme.jnsbbk.oauthserver.utils

import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*

/** Provides a Kotlin-idiomatic wrapper for getting a value from an [Optional]. */
fun <T> Optional<T>.getOrNull(): T? = if (isPresent) get() else null

/** Provides an easy access for the base URL of the server deployment. */
fun getServerBaseUrl() = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()

/** Provides an easy to read wrapper for multiple null checks. */
fun anyNotNull(vararg things: Any?): Boolean {
    things.forEach { if (it != null) return true }
    return false
}