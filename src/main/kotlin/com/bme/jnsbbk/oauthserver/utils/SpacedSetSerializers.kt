package com.bme.jnsbbk.oauthserver.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class SpacedSetSerializer : JsonSerializer<Set<String>>() {
    override fun serialize(value: Set<String>, gen: JsonGenerator, provider: SerializerProvider) =
        gen.writeString(value.joinToString(" "))
}

class SpacedSetDeserializer : JsonDeserializer<Set<String>>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Set<String> =
        parser.text.split(" ").toSet()
}