package com.bme.jnsbbk.oauthserver.utils

import javax.persistence.AttributeConverter
import javax.persistence.Converter

/**
 * Provides serialization methods for string sets.
 *
 * Automatically applies to every string set used in databases. Certain IDEs don't take
 * this into account and may show errors in entity classes.
 */
@Converter(autoApply = true)
class StringSetConverter : AttributeConverter<Set<String>, String> {
    companion object { const val SEPARATOR = ',' }

    override fun convertToDatabaseColumn(set: Set<String>) = set.joinToString(SEPARATOR.toString())

    override fun convertToEntityAttribute(str: String) = str.split(SEPARATOR).toMutableSet()
}