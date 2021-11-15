package com.bme.jnsbbk.oauthserver.utils

import org.springframework.web.util.UriComponentsBuilder

/** A multipurpose URL class that wraps the [UriComponentsBuilder] interface. */
class URL(
    /** The base URL string. */
    var base: String = "",
    /** A string map of potential query parameters. Keys with null values will be ignored. */
    var params: MutableMap<String, String?> = mutableMapOf(),
    /** A string map of potential fragment fields. Keys with null values will be ignored. */
    var fragments: MutableMap<String, String?> = mutableMapOf()
) {

    /** Builder function, adds all [additional] elements to the query parameter list. */
    fun withParams(additional: Map<String, String?>): URL {
        params.putAll(additional)
        return this
    }

    /** Builder function, adds a [key]-[value] pair to the query parameter list. */
    fun withParam(key: String, value: String?): URL {
        params[key] = value
        return this
    }

    /** Builder function, adds all [additional] elements to the fragment field list. */
    fun withFragments(additional: Map<String, String?>): URL {
        fragments.putAll(additional)
        return this
    }

    /** Builder function, adds a [key]-[value] pair to the fragment field list. */
    fun withFragment(key: String, value: String?): URL {
        fragments[key] = value
        return this
    }

    /** Creates an URL string from the fields stored in the URL object. */
    fun build(): String {
        val builder = UriComponentsBuilder.fromUriString(base)
        params.forEach { (key, value) ->
            if (value != null) builder.queryParam(key, value)
        }

        val fragmentBuilder = UriComponentsBuilder.fromUriString("")
        fragments.forEach { (key, value) ->
            if (value != null) fragmentBuilder.queryParam(key, value)
        }
        val fragment = fragmentBuilder.toUriString().removePrefix("?")
        if (fragment != "") builder.fragment(fragment)

        return builder.toUriString()
    }

    /** Creates a redirection URL string from the field stored in the URL object. */
    fun redirect(): String {
        return "redirect:" + build()
    }
}
