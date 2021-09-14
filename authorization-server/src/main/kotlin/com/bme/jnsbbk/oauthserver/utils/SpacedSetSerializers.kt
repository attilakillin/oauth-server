package com.bme.jnsbbk.oauthserver.utils

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

/** Serializes string sets into space-delimited strings. */
class SpacedSetSerializer : JsonSerializer<Set<String>>() {
    override fun serialize(value: Set<String>, gen: JsonGenerator, provider: SerializerProvider) =
        gen.writeString(value.joinToString(" "))
}

/** Deserializes space-delimited strings into string sets. */
class SpacedSetDeserializer : JsonDeserializer<Set<String>>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Set<String> =
        parser.text.split(" ").toSet()
}
