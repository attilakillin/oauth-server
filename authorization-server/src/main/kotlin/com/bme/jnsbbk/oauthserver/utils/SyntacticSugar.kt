package com.bme.jnsbbk.oauthserver.utils

import com.bme.jnsbbk.oauthserver.config.AppConfig
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import java.util.*

@Component
object AppContext : ApplicationContextAware {
    lateinit var context: ApplicationContext private set

    override fun setApplicationContext(context: ApplicationContext) { this.context = context }

}

fun getIssuerString(): String = AppContext.context.getBean<AppConfig>(AppConfig::class).issuerString

/** Provides an easy-to-read wrapper for multiple null checks. */
fun anyNotNull(vararg things: Any?) = things.any { it != null }

/** Provides an easy-to-read wrapper for multiple truth checks. */
fun anyTrue(vararg things: Boolean) = things.any { it }

/** Method to find a key of a given [Map] with the given [value]. */
fun <K, V> Map<K, V>.findKey(value: V): K? = filterValues { it == value }.keys.firstOrNull()

/**
 * Decode a string as if it was an HTTP Basic header string.
 *
 * Decodes a Base64-encoded string with a 'value:value' pattern. Returns the two values as a nullable pair.
 */
fun String.decodeAsHttpBasic(): Pair<String, String>? {
    if (!startsWith("Basic ")) return null
    val content = Base64.getUrlDecoder()
        .decode(removePrefix("Basic "))
        .toString(Charsets.UTF_8)
    if (!content.contains(':')) return null
    return Pair(content.substringBefore(':'), content.substringAfter(':'))
}
