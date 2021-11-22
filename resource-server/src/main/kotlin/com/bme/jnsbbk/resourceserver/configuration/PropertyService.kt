package com.bme.jnsbbk.resourceserver.configuration

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PropertyService(
    private val propertyRepository: PropertyRepository
) {

    /** Persists a property with the given [key] and [value]. */
    fun saveProperty(key: Property.Key, value: String) {
        propertyRepository.save(Property(key, value))
    }

    /** Saves an ID and a secret as two separate properties. */
    fun saveConfiguration(id: String, secret: String) {
        saveProperty(Property.Key.ID, id)
        saveProperty(Property.Key.SECRET, secret)
    }

    /** Retrieves a given property. Might return null. */
    fun getProperty(key: Property.Key): String? {
        return propertyRepository.findByIdOrNull(key)?.value
    }
}
