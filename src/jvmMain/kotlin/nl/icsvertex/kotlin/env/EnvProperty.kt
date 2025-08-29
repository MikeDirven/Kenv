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
                System.getenv(it)?.let {
                    put("value", it)
                }
            } ?: property.name.let {
                System.getenv(it)?.let {
                    put("value", it)
                }
            }
            listNameFromAnnotation?.let {
                System.getenv(it)?.let {
                    put("value", it)
                }
            }
            name?.let {
                System.getenv(it)?.let {
                    put("value", it)
                }
            }
        }.ifEmpty { null }
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
                properties.getOrNull(property)?.let {
                    put("value", it)
                }
            } ?: property.name.let { property ->
                properties.getOrNull(property)?.let {
                    put("value", it)
                }
            }
            listNameFromAnnotation?.let { property ->
                (properties.getOrNull(property) ?: defaultFromAnnotation)?.let {
                    put(
                        "value",
                        JsonArray(
                            it.split(",").map { JsonPrimitive(it.trim()) }
                        )
                    )
                }
            }
            name?.let { property ->
                properties.getOrNull(property)?.let {
                    put("value", it)
                }
            }
        }
    } catch (e: Exception) {
        null
    }

    // Try to deserialize the environment file property
    if (configProperty?.get("value") != null) try {
        return defaultJsonSerializer.decodeFromJsonElement<PropertyValue<R>>(configProperty).value
    } catch (e: Exception) {}

    // Last resort try default from annotation
    defaultFromAnnotation?.let {
        return defaultJsonSerializer.decodeFromJsonElement<PropertyValue<R>>(
            buildJsonObject {
                put("value", it)
            }
        ).value
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
                println("Loading environment properties from file: environment.ini")
                File("environment.ini" ).let { iniFile ->
                    Properties().apply {
                        iniFile.inputStream().use {
                            this.load(it.reader(Charsets.UTF_8))
                        }

                        properties.putAll(
                            this.entries.map {
                                println("Loading property: ${it.key} = ${it.value}")
                                Pair(it.key.toString(), it.value.toString())
                            }
                        )
                    }
                }
                println("Loaded ${properties.get().size} environment properties")
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