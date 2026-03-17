package io.github.mikedirven.exceptions

import kotlin.reflect.KClass

class MissingSerializerException(kClass: KClass<*>) : Exception(
    "Missing serializer for class: ${kClass.simpleName ?: kClass.toString()}"
)