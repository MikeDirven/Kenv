package nl.icsvertex.kotlin.env

import kotlinx.browser.window
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import nl.icsvertex.kotlin.env.EnvProperty.Companion.properties
import nl.icsvertex.kotlin.env.atomic.AtomicMap
import nl.icsvertex.kotlin.env.classes.PropertyValue
import nl.icsvertex.kotlin.env.exceptions.PropertyNotFoundException
import nl.icsvertex.kotlin.env.extensions.callableName
import nl.icsvertex.kotlin.env.interfaces.Serializer
import nl.icsvertex.kotlin.env.serializers.DefaultSerializers
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
        }
    } catch (e: Exception){
        null
    }

    // Try to deserialize the system property
    if(envProperty != null) try {
        return Json.decodeFromJsonElement<PropertyValue<R>>(envProperty).value
    } catch (e: Exception){
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
//    actual operator fun getValue(thisRef: Any?, property: KProperty<*>): R {
//        // First check if the system contains the property
//        val envProperty: JsonObject? = try {
//            buildJsonObject {
//                name?.let {
//                    properties.getOrNull(it)?.let {
//                        put("value", it)
//                    }
//                }
//                property.name.let {
//                    properties.getOrNull(it)?.let {
//                        put("value", it)
//                    }
//                }
//            }
//        } catch (e: Exception){
//            null
//        }
//
//        // Try to deserialize the system property
//        if(envProperty != null) try {
//            return Json.decodeFromJsonElement<PropertyValue<R>>(envProperty).value
//        } catch (e: Exception){
//            e.printStackTrace()
//        }
//
//        return default
//            ?: throw PropertyNotFoundException(
//                name ?: property.name
//            )
//    }


    actual companion object {
        actual val serializers: AtomicMap<KClass<*>, Serializer> = AtomicMap(
            mapOf(
                Any::class to DefaultSerializers.DefaultSerializer,
                String::class to DefaultSerializers.StringSerializer,
                Int::class to DefaultSerializers.IntSerializer
            )
        )
        actual val properties: AtomicMap<String, String> = AtomicMap()

        actual fun registerSerializer(kClass: KClass<*>, serializer: Serializer){
            serializers.put(kClass, serializer)
        }

        actual inline fun <reified T> registerSerializer(serializer: Serializer) {
            registerSerializer(T::class, serializer)
        }

        init {
            // Read environment on first initialization of the class
            if(properties.isEmpty) readEnvironment()
        }

        @OptIn(ExperimentalSerializationApi::class)
        internal actual fun readEnvironment() = try {
            val json = Json.decodeFromDynamic<JsonObject>(window.asDynamic().environment)
            json.forEach {
                val key = it.key
                val value = it.value.jsonPrimitive.content

                properties.put(key to value)
            }
            window.asDynamic().environment = null
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