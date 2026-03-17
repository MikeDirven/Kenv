package io.github.mikedirven

import io.github.mikedirven.atomic.AtomicMap
import io.github.mikedirven.classes.PropertyValue
import io.github.mikedirven.exceptions.PropertyNotFoundException
import io.github.mikedirven.serializers.defaultJsonSerializer
import io.github.mikedirven.utils.isArray
import io.github.mikedirven.utils.isObject
import io.github.mikedirven.window.Window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
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
            if (property.name.isBlank()) {
                EnvProperty.Companion.properties.getOrNull(property.name)?.let { propertyValue ->
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
    } catch (e: Exception) {}

    return default ?: throw PropertyNotFoundException(
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


        @OptIn(ExperimentalSerializationApi::class, ExperimentalWasmJsInterop::class)
        internal actual fun readEnvironment(path: String?) = try {
            println("Reading environment variables...")
            val environmentObject = Window.environment

            // 1. Get all the keys from the JavaScript object.
            val keys: Array<JsString> = getObjectKeys(environmentObject).toArray()
            keys.forEach { key ->
                getProperty(environmentObject, key)?.let {
                    properties.put(key.toString() to it.toString())
                } ?: println("Failed to load environment property: $key")
            }

            println("Loaded ${properties.getValue().size} environment properties")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Unable to serialize environment object!")
            println("Current environment string: ${Window.environment}")
        }

        actual inline operator fun <reified R: Any> invoke(name: String?, default: R?) : EnvProperty<R> {
            return EnvProperty<R>(R::class, name, default)
        }

        actual inline operator fun <reified R: Any> invoke() : EnvProperty<R> {
            return EnvProperty<R>(R::class, null, null)
        }
    }
}

@JsFun("Object.keys")
@OptIn(ExperimentalWasmJsInterop::class)
private external fun getObjectKeys(obj: JsAny): JsArray<JsString>

@JsFun("(obj, key) => obj[key]")
@OptIn(ExperimentalWasmJsInterop::class)
private external fun getProperty(obj: JsAny, key: JsString): JsAny?