package com.bme.jnsbbk.oauthserver.utils

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class StringSetConverter : AttributeConverter<MutableSet<String>, String> {
    override fun convertToDatabaseColumn(set: MutableSet<String>) = set.joinToString(separator = ",")

    override fun convertToEntityAttribute(str: String) = str.split(",").toMutableSet()
}