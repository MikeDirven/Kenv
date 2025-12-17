package nl.icsvertex.kotlin.env

import nl.icsvertex.kotlin.env.atomic.AtomicMap
import nl.icsvertex.kotlin.env.interfaces.Serializer
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

expect inline operator fun <reified R : Any> EnvProperty<R>.getValue(thisRef: Any?, property: KProperty<*>) : R

/**
 * A property delegate for accessing environment variables with type safety and default values.
 * This class provides a convenient way to read environment variables and convert them to the desired type.
 *
 * To load extra environment files use the property `ENV_FILE`
 *
 * @param R The type of the property value, must be a non-null type
 * @param kClass The Kotlin class representing the type R
 * @param name The name of the environment variable to read. If null, the property name will be used
 * @param default The default value to use if the environment variable is not found or cannot be converted
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class EnvProperty<R: Any>(
    kClass: KClass<R>,
    name: String?,
    default: R?
) {
//    operator fun getValue(thisRef: Any?, property: KProperty<*>): R

    companion object {
        val properties: AtomicMap<String, String>
        val ENV_FILE_PROPERTY: String
        
        /**
         * Reads environment variables from the system or from a specified file.
         *
         * If the `ENV_FILE` property is set, it will be used as the path to the environment file to read from first.
         *
         * @param path The path to the environment file to read from. If null, tries to read de default `environment.ini` or the system environment.
         */
        internal fun readEnvironment(path: String? = null)

        /**
         * Creates an EnvProperty instance with a specified name and optional default value.
         * The type R is inferred from the reified type parameter.
         *
         * @param R The type of the property value, must be a non-null type
         * @param name The name of the environment variable to read. If null, the property name will be used
         * @param default The default value to use if the environment variable is not found or cannot be converted
         * @return A new EnvProperty instance configured for the specified type and parameters
         */
        inline operator fun <reified R: Any> invoke(name: String?, default: R? = null) : EnvProperty<R>

        /**
         * Creates an EnvProperty instance without a default value.
         * The type R is inferred from the reified type parameter and the property name will be used as the environment variable name.
         *
         * @param R The type of the property value, must be a non-null type
         * @return A new EnvProperty instance configured for the specified type
         */
        inline operator fun <reified R: Any> invoke() : EnvProperty<R>
    }
}