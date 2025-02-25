package nl.icsvertex.kotlin.env

import kotlinx.serialization.json.*
import nl.icsvertex.kotlin.env.EnvProperty.Companion.properties
import nl.icsvertex.kotlin.env.annotations.EnvironmentDefault
import nl.icsvertex.kotlin.env.annotations.EnvironmentListProperty
import nl.icsvertex.kotlin.env.annotations.EnvironmentProperty
import nl.icsvertex.kotlin.env.atomic.AtomicMap
import nl.icsvertex.kotlin.env.classes.PropertyValue
import nl.icsvertex.kotlin.env.exceptions.PropertyNotFoundException
import nl.icsvertex.kotlin.env.interfaces.Serializer
import nl.icsvertex.kotlin.env.serializers.defaultJsonSerializer
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

inline operator fun <reified R: Any> EnvProperty<R>.getValue(thisRef: Any?, property: KProperty<*>): R {
    val nameFromAnnotation = property.findAnnotation<EnvironmentProperty>()?.name
    val listNameFromAnnotation = property.findAnnotation<EnvironmentListProperty>()?.name
    val defaultFromAnnotation = property.findAnnotation<EnvironmentDefault>()?.value

    // First check if the system contains the property
    val systemProperty: JsonObject? = try {
        buildJsonObject {
            nameFromAnnotation?.also {
                put("value", System.getenv(it) ?: defaultFromAnnotation)
            } ?: property.name.let {
                put("value", System.getenv(it) ?: defaultFromAnnotation)
            }
            listNameFromAnnotation?.let {
                put("value", System.getenv(it) ?: defaultFromAnnotation)
            }
            name?.let {
                put("value", System.getenv(it) ?: defaultFromAnnotation)
            }
        }
    } catch (e: Exception) {
        null
    }

    // Try to deserialize the system property
    if (systemProperty != null) try {
        return defaultJsonSerializer.decodeFromJsonElement<PropertyValue<R>>(
            systemProperty
        ).value
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Then check for property in environment file
    val configProperty: JsonObject? = try {
        buildJsonObject {
            nameFromAnnotation?.also { property ->
                put("value", properties.getOrNull(property) ?: defaultFromAnnotation)
            } ?: property.name.let { property ->
                put("value", properties.getOrNull(property) ?: defaultFromAnnotation)
            }
            listNameFromAnnotation?.let { property ->
                put(
                    "value",
                    JsonArray(
                        (properties.getOrNull(property) ?: defaultFromAnnotation)?.split(",")!!
                            .map { JsonPrimitive(it.trim()) }
                    )

                )
            }
            name?.let { property ->
                put("value", properties.getOrNull(property) ?: defaultFromAnnotation)
            }
        }
    } catch (e: Exception) {
        null
    }

    // Try to deserialize the environment file property
    if (configProperty != null) try {
        return defaultJsonSerializer.decodeFromJsonElement<PropertyValue<R>>(configProperty).value
    } catch (e: Exception) {
        e.printStackTrace()
    }

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
        actual val properties: AtomicMap<String, String> = AtomicMap()

        init {
            // Read environment on first initialization of the class
            if(properties.isEmpty) readEnvironment()
        }

        actual fun readEnvironment() {
            // Get ini file
            try {
                File("environment.ini" ).let { iniFile ->
                    Properties().apply {
                        iniFile.inputStream().use {
                            this.load(it.reader(Charsets.UTF_8))
                        }

                        properties.putAll(
                            this.entries.map { Pair(it.key.toString(), it.value.toString()) }
                        )
                    }
                }
            } catch (e: Exception) {
                null
            }
        }

        actual inline operator fun <reified R: Any> invoke(name: String?, default: R?)
                = EnvProperty<R>(R::class, name, default)

        actual inline operator fun <reified R: Any> invoke()
                = EnvProperty<R>(R::class, null, null)
    }
}