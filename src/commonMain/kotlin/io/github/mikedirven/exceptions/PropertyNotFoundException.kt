package io.github.mikedirven.exceptions

class PropertyNotFoundException(propertyName: String) : Exception(
    "Property not found: $propertyName"
)