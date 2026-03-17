package io.github.mikedirven.atomic.exceptions

class PropertyNotFoundException(propertyName: String) : Exception(
    "Property not found: $propertyName"
)