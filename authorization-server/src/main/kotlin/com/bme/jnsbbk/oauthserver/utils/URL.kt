package com.bme.jnsbbk.oauthserver.utils

import org.springframework.web.util.UriComponentsBuilder

class URL(
    var base: String = "",
    var params: MutableMap<String, String?> = mutableMapOf(),
    var fragments: MutableMap<String, String?> = mutableMapOf()
) {

    fun withParams(additional: Map<String, String?>): URL {
        params.putAll(additional)
        return this
    }

    fun withParam(key: String, value: String?): URL {
        params[key] = value
        return this
    }

    fun withFragments(additional: Map<String, String?>): URL {
        fragments.putAll(additional)
        return this
    }

    fun withFragment(key: String, value: String?): URL {
        fragments[key] = value
        return this
    }

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

    fun redirect(): String {
        return "redirect:" + build()
    }
}
