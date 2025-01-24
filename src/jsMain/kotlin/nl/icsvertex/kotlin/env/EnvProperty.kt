package nl.icsvertex.kotlin.env

import kotlinx.browser.window
import kotlinx.serialization.json.*
import nl.icsvertex.kotlin.env.atomic.AtomicMap
import nl.icsvertex.kotlin.env.exceptions.MissingSerializerException
import nl.icsvertex.kotlin.env.exceptions.PropertyNotFoundException
import nl.icsvertex.kotlin.env.interfaces.Serializer
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class EnvProperty<R: Any> actual constructor(
    private val kClass: KClass<R>,
    private val name: String?,
    private val default: R?
) {
    actual operator fun getValue(thisRef: Any?, property: KProperty<*>): R {
        val registeredSerializer: Serializer = serializers.getOrNull(kClass)
            ?: serializers.getOrNull(Any::class)
            ?: throw MissingSerializerException(kClass)


        // First check if the system contains the property
        val envProperty: JsonObject? = try {
            buildJsonObject {
                name?.let {
                    properties.getOrNull(it)?.let {
                        put("value", it)
                    }
                }
                property.name.let {
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
            return registeredSerializer.deserialize<R>(
                envProperty
            ).value
        } catch (e: Exception){
            e.printStackTrace()
        }

        return default
            ?: throw PropertyNotFoundException(
                name ?: property.name
            )
    }


    actual companion object {
        internal actual val serializers: AtomicMap<KClass<*>, Serializer> = AtomicMap()
        internal actual val properties: AtomicMap<String, String> = AtomicMap()

        actual fun registerSerializer(kClass: KClass<*>, serializer: Serializer){
            serializers.put(kClass, serializer)
        }

        actual inline fun <reified T> registerSerializer(serializer: Serializer) {
            registerSerializer(T::class, serializer)
        }

        internal actual fun readEnvironment() {
            val json = window.asDynamic().environment as JsonObject
            json.forEach {
                val key = it.key
                val value = it.value.jsonPrimitive.content

                properties.put(key to value)
            }
        }

        actual inline operator fun <reified R: Any> invoke(name: String?, default: R?) : EnvProperty<R> {
            return EnvProperty<R>(R::class, name, default)
        }

        actual inline operator fun <reified R: Any> invoke() : EnvProperty<R> {
            return EnvProperty<R>(R::class, null, null)
        }
    }
}