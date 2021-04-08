package com.bme.jnsbbk.oauthserver.utils

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = true)
class StringSetConverter : AttributeConverter<MutableSet<String>, String> {
    companion object { const val SEPARATOR = ',' }

    override fun convertToDatabaseColumn(set: MutableSet<String>) = set.joinToString(SEPARATOR.toString())

    override fun convertToEntityAttribute(str: String) = str.split(SEPARATOR).toMutableSet()
}