package nl.icsvertex.kotlin.env.exceptions

import kotlin.reflect.KClass

class PropertyNotFoundException(propertyName: String) : Exception(
    "Property not found: $propertyName"
)