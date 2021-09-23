package com.bme.jnsbbk.oauthserver.utils

import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.util.*

/** Provides a Kotlin-idiomatic wrapper for getting a value from an [Optional]. */
fun <T> Optional<T>.getOrNull(): T? = if (isPresent) get() else null

/**
 * Provides easy access to the base URL of the server.
 *
 * This function can throw an exception if used outside a Spring component,
 * since there is no current context to query the path from.
 */
fun getServerBaseUrl() = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()

/** Provides an easy-to-read wrapper for multiple null checks. */
fun anyNotNull(vararg things: Any?) = things.any { it != null }

/** Provides an easy-to-read wrapper for multiple truth checks. */
fun anyTrue(vararg things: Boolean) = things.any { it }

/** Method to find a key of a given [Map] with the given [value]. */
fun <K, V> Map<K, V>.findKey(value: V) = filterValues { it == value }.keys.first()
