package io.github.mikedirven

import io.github.mikedirven.atomic.AtomicMap
import io.github.mikedirven.classes.PropertyValue
import io.github.mikedirven.exceptions.PropertyNotFoundException
import io.github.mikedirven.extensions.callableName
import io.github.mikedirven.serializers.defaultJsonSerializer
import io.github.mikedirven.utils.asArray
import io.github.mikedirven.utils.asObject
import io.github.mikedirven.utils.asPrimitive
import io.github.mikedirven.utils.isArray
import io.github.mikedirven.utils.isObject
import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

actual inline operator fun <reified R : Any> EnvProperty<R>.getValue(thisRef: Any?, property: KProperty<*>) : R {
    // First check if the system contains the property
    val envProperty: JsonObject? = try {
        buildJsonObject {
            name?.let {
                EnvProperty.Companion.properties.getOrNull(it)?.let { propertyValue ->
                    propertyValue.isObject()?.let { propertyObject ->
                        put("value", propertyObject)
                    } ?: propertyValue.isArray()?.let { propertyArray ->
                        put("value", propertyArray)
                    } ?: put("value", propertyValue)
                }
            }
            if (property.name.isNotBlank()) {
                EnvProperty.Companion.properties.getOrNull(property.name)?.let { propertyValue ->
                    propertyValue.isObject()?.let { propertyObject ->
                        put("value", propertyObject)
                    } ?: propertyValue.isArray()?.let { propertyArray ->
                        put("value", propertyArray)
                    } ?: put("value", propertyValue)
                }
            }
            property.callableName.let {
                EnvProperty.Companion.properties.getOrNull(it)?.let { propertyValue ->
                    propertyValue.isObject()?.let { propertyObject ->
                        put("value", propertyObject)
                    } ?: propertyValue.isArray()?.let { propertyArray ->
                        put("value", propertyArray)
                    } ?: put("value", propertyValue)
                }
            }
        }.ifEmpty { null }
    } catch (e: Exception){
        null
    }

    // Try to deserialize the system property
    if(envProperty?.get("value") != null) try {
        return defaultJsonSerializer.decodeFromJsonElement<PropertyValue<R>>(envProperty).value
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return default
        ?: throw PropertyNotFoundException(
            name ?: property.name
        )
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class EnvProperty<R: Any> actual constructor(
    val kClass: KClass<R>,
    val name: String?,
    val default: R?
) {
    actual companion object {
        actual const val ENV_FILE_PROPERTY = "ENV_FILE"
        actual val properties: AtomicMap<String, String> = AtomicMap()

        init {
            // Read environment on first initialization of the class
            if(properties.isEmpty) readEnvironment()
        }

        @OptIn(ExperimentalSerializationApi::class)
        internal actual fun readEnvironment(path: String?) = try {
            console.log("Reading environment variables...")
            val json = defaultJsonSerializer.decodeFromDynamic<JsonObject>(window.asDynamic().environment)
            json.forEach { (key, value) ->
                val key = key
                val value = value.asPrimitive()
                    ?: value.asObject()
                    ?: value.asArray()

                value?.let { loadedValue ->
                    properties.put(key to loadedValue)
                    println("Loaded environment property: $key -> $value")
                } ?: println("Failed to load environment property: $key -> $value")
            }

            window.asDynamic().environment = null
            console.log("Loaded ${properties.getValue().size} environment properties")
        } catch (e: Exception) {
            e.printStackTrace()
            console.log("Unable to serialize environment object!")
        }

        actual inline operator fun <reified R: Any> invoke(name: String?, default: R?) : EnvProperty<R> {
            return EnvProperty<R>(R::class, name, default)
        }

        actual inline operator fun <reified R: Any> invoke() : EnvProperty<R> {
            return EnvProperty<R>(R::class, null, null)
        }
    }
}