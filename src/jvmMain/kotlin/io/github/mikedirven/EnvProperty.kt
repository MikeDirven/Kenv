package io.github.mikedirven

import io.github.mikedirven.atomic.AtomicMap
import io.github.mikedirven.annotations.EnvironmentDefault
import io.github.mikedirven.annotations.EnvironmentListProperty
import io.github.mikedirven.annotations.EnvironmentProperty
import io.github.mikedirven.classes.PropertyValue
import io.github.mikedirven.exceptions.PropertyNotFoundException
import io.github.mikedirven.serializers.defaultJsonSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import java.io.File
import java.util.Properties
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

actual inline operator fun <reified R: Any> EnvProperty<R>.getValue(thisRef: Any?, property: KProperty<*>): R {
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
                EnvProperty.Companion.properties.getOrNull(property)?.let {
                    put("value", it)
                }
            } ?: property.name.let { property ->
                EnvProperty.Companion.properties.getOrNull(property)?.let {
                    put("value", it)
                }
            }
            listNameFromAnnotation?.let { property ->
                (EnvProperty.Companion.properties.getOrNull(property) ?: defaultFromAnnotation)?.let {
                    put(
                        "value",
                        JsonArray(
                            it.split(",").map { JsonPrimitive(it.trim()) }
                        )
                    )
                }
            }
            name?.let { property ->
                EnvProperty.Companion.properties.getOrNull(property)?.let {
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
        actual const val ENV_FILE_PROPERTY = "ENV_FILE"
        actual val properties: AtomicMap<String, String> = AtomicMap()

        init {
            // Read environment on first initialization of the class
            if(properties.isEmpty) readEnvironment()
        }

        actual fun readEnvironment(path: String?) {
            // Get ini file
            try {
                File(path ?: "environment.ini").let { iniFile ->
                    Properties().apply {
                        iniFile.inputStream().use {
                            this.load(it.reader(Charsets.UTF_8))
                        }

                        this.entries.firstOrNull { (property, value) ->
                            property == ENV_FILE_PROPERTY && value?.toString()?.isNotBlank() ?: false
                        }?.let { readEnvironment(it.value.toString()) }

                        println("Loading environment properties from file: ${path ?: "environment.ini" }")

                        properties.putAll(
                            this.entries.filter { it.key != ENV_FILE_PROPERTY }.map { (property, value) ->
                                println("Loading property: ${property} = ${value}")
                                Pair(property.toString(), value.toString())
                            }
                        )
                    }
                }
                println("Loaded ${properties.get().size} environment properties")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        actual inline operator fun <reified R: Any> invoke(name: String?, default: R?)
                = EnvProperty<R>(R::class, name, default)

        actual inline operator fun <reified R: Any> invoke()
                = EnvProperty<R>(R::class, null, null)
    }
}