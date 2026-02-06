package nl.icsvertex.kotlin.env

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import nl.icsvertex.kotlin.env.EnvProperty.Companion.properties
import nl.icsvertex.kotlin.env.atomic.AtomicMap
import nl.icsvertex.kotlin.env.classes.PropertyValue
import nl.icsvertex.kotlin.env.exceptions.PropertyNotFoundException
import nl.icsvertex.kotlin.env.serializers.defaultJsonSerializer
import nl.icsvertex.kotlin.env.utils.asArray
import nl.icsvertex.kotlin.env.utils.asObject
import nl.icsvertex.kotlin.env.utils.asPrimitive
import nl.icsvertex.kotlin.env.utils.isArray
import nl.icsvertex.kotlin.env.utils.isObject
import nl.icsvertex.kotlin.env.window.Window
import kotlin.js.unsafeCast
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

actual inline operator fun <reified R : Any> EnvProperty<R>.getValue(thisRef: Any?, property: KProperty<*>) : R {
    // First check if the system contains the property
    val envProperty: JsonObject? = try {
        buildJsonObject {
            name?.let {
                properties.getOrNull(it)?.let { propertyValue ->
                    propertyValue.isObject()?.let { propertyObject ->
                        put("value", propertyObject)
                    } ?: propertyValue.isArray()?.let { propertyArray ->
                        put("value", propertyArray)
                    }?: put("value", propertyValue)
                }
            }
            if(property.name.isBlank()) {
                properties.getOrNull(property.name)?.let { propertyValue ->
                    propertyValue.isObject()?.let { propertyObject ->
                        put("value", propertyObject)
                    } ?: propertyValue.isArray()?.let { propertyArray ->
                        put("value", propertyArray)
                    }?: put("value", propertyValue)
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
            val resultMap = mutableMapOf<String, String>()

            // 1. Get all the keys from the JavaScript object.
            val keys: Array<JsString> = getObjectKeys(environmentObject).toArray()
            keys.forEach { key ->
                val value = getProperty(environmentObject, key)
                value?.let {
                    properties.put(key.toString() to it.toString())
                    println("Loaded environment property: $key -> $value")
                } ?: println("Failed to load environment property: $key -> $value")
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