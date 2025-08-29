package nl.icsvertex.kotlin.env

import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import nl.icsvertex.kotlin.env.EnvProperty.Companion.properties
import nl.icsvertex.kotlin.env.atomic.AtomicMap
import nl.icsvertex.kotlin.env.classes.PropertyValue
import nl.icsvertex.kotlin.env.exceptions.PropertyNotFoundException
import nl.icsvertex.kotlin.env.extensions.callableName
import nl.icsvertex.kotlin.env.serializers.defaultJsonSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

inline operator fun <reified R : Any> EnvProperty<R>.getValue(thisRef: Any?, property: KProperty<*>) : R {
    // First check if the system contains the property
    val envProperty: JsonObject? = try {
        buildJsonObject {
            name?.let {
                properties.getOrNull(it)?.let {
                    put("value", it)
                }
            }
            if(property.name.isBlank()) {
                properties.getOrNull(property.name)?.let {
                    put("value", it)
                }
            }
            property.callableName.let {
                properties.getOrNull(it)?.let {
                    put("value", it)
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
        actual val properties: AtomicMap<String, String> = AtomicMap()

        init {
            // Read environment on first initialization of the class
            if(properties.isEmpty) readEnvironment()
        }

        @OptIn(ExperimentalSerializationApi::class)
        internal actual fun readEnvironment() = try {
            console.log("Reading environment variables...")
            val json = defaultJsonSerializer.decodeFromDynamic<JsonObject>(window.asDynamic().environment)
            json.forEach {
                val key = it.key
                val value = it.value.jsonPrimitive.content

                properties.put(key to value)

                console.log("Loaded environment property: $key -> $value")
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