package com.bme.jnsbbk.oauthserver.utils

import com.bme.jnsbbk.oauthserver.config.AppConfig
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import java.util.*

/**
 * A singleton application context class. Used in [getIssuerString] below.
 *
 * This component is aware of the application context, and stores it in a publicly available field.
 */
@Component
object AppContext : ApplicationContextAware {
    lateinit var context: ApplicationContext private set

    override fun setApplicationContext(context: ApplicationContext) { this.context = context }

}

/** Returns the issuer string as configured in the application properties file. */
fun getIssuerString(): String = AppContext.context.getBean<AppConfig>(AppConfig::class).issuerString

/** Extension function, the opposite of [isNullOrEmpty]. */
fun <T> Collection<T>?.isNotNullOrEmpty(): Boolean = !isNullOrEmpty()

/**
 * Extension function, returns true if the collection is either null,
 * or all elements match the given [predicate].
 */
fun <T> Collection<T>?.isNullOrAll(predicate: (T) -> Boolean): Boolean = this == null || all(predicate)

/** Extension function, returns true if the object is either null, or it matches the given [predicate]. */
fun <T> T?.isNullOr(predicate: (T) -> Boolean): Boolean = this == null || predicate(this)

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
