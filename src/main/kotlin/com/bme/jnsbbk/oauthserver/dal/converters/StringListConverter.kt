package com.bme.jnsbbk.oauthserver.dal.converters

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {
    override fun convertToDatabaseColumn(list: List<String>) = list.joinToString(separator = ",")

    override fun convertToEntityAttribute(str: String) = str.split(",")
}