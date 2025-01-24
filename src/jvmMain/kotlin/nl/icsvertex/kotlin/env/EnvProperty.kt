package nl.icsvertex.kotlin.env

import kotlinx.serialization.json.*
import nl.icsvertex.kotlin.env.annotations.EnvironmentDefault
import nl.icsvertex.kotlin.env.annotations.EnvironmentListProperty
import nl.icsvertex.kotlin.env.annotations.EnvironmentProperty
import nl.icsvertex.kotlin.env.atomic.AtomicMap
import nl.icsvertex.kotlin.env.exceptions.MissingSerializerException
import nl.icsvertex.kotlin.env.exceptions.PropertyNotFoundException
import nl.icsvertex.kotlin.env.interfaces.Serializer
import java.io.File
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class EnvProperty<R: Any> actual constructor(
    private val kClass: KClass<R>,
    private val name: String?,
    private val default: R?
) {
    actual operator fun getValue(thisRef: Any?, property: KProperty<*>): R {
        val nameFromAnnotation = property.findAnnotation<EnvironmentProperty>()?.name
        val listNameFromAnnotation = property.findAnnotation<EnvironmentListProperty>()?.name
        val defaultFromAnnotation = property.findAnnotation<EnvironmentDefault>()?.value
        val registeredSerializer: Serializer = serializers.getOrNull(kClass)
            ?: serializers.getOrNull(Any::class)
            ?: throw MissingSerializerException(kClass)

        // First check if the system contains the property
        val systemProperty: JsonObject? = try {
            buildJsonObject {
                nameFromAnnotation?.let {
                    put("value", System.getenv(it) ?: defaultFromAnnotation)
                }
                listNameFromAnnotation?.let {
                    put("value", System.getenv(it)?: defaultFromAnnotation)
                }
                name?.let {
                    put("value", System.getenv(it)?: defaultFromAnnotation)
                }
                property.name?.let {
                    put("value", System.getenv(it)?: defaultFromAnnotation)
                }
            }
        } catch (e: Exception){
            null
        }

        // Try to deserialize the system property
        if(systemProperty != null) try {
            return registeredSerializer.deserialize<R>(
                systemProperty
            ).value
        } catch (e: Exception){
            e.printStackTrace()
        }

        // Then check for property in environment file
        val configProperty: JsonObject? = try {
            buildJsonObject {
                nameFromAnnotation?.let { property ->
                    put("value", properties.getOrNull(property) ?: defaultFromAnnotation)
                }
                listNameFromAnnotation?.let { property ->
                    put(
                        "value",
                        JsonArray(
                            (properties.getOrNull(property) ?: defaultFromAnnotation)?.split(",")!!.map { JsonPrimitive(it.trim()) }
                        )

                    )
                }
                name?.let { property ->
                    put("value", properties.getOrNull(property) ?: defaultFromAnnotation)
                }
                property.name.let { property ->
                    put("value", properties.getOrNull(property) ?: defaultFromAnnotation)
                }
            }
        } catch (e: Exception){
            null
        }

        // Try to deserialize the environment file property
        if(configProperty != null) try {
            return registeredSerializer.deserialize<R>(
                configProperty
            ).value
        } catch (e: Exception){
            e.printStackTrace()
        }

        // Return the default value if available else property is not found
        return default
            ?: throw PropertyNotFoundException(
                name ?: nameFromAnnotation ?: property.name
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

        init {
            // Read environment on first initialization of the class
            if(properties.isEmpty) readEnvironment()
        }

        internal actual fun readEnvironment() {
            // Get ini file
            val iniFile: File? = System.getenv("APPDATA")?.let { File(it, ) }

            // load ini properties
            iniFile?.let {
                Properties().apply {
                    iniFile.inputStream().use {
                        this.load(it.reader(Charsets.UTF_8))
                    }

                    properties.putAll(
                        this.entries.map { Pair(it.key.toString(), it.value.toString()) }
                    )
                }
            }
        }

        actual inline operator fun <reified R: Any> invoke(name: String?, default: R?)
                = EnvProperty<R>(R::class, name, default)

        actual inline operator fun <reified R: Any> invoke()
                = EnvProperty<R>(R::class, null, null)
    }
}