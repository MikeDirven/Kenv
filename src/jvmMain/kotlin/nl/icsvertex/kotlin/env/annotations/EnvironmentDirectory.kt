package nl.icsvertex.kotlin.env.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class EnvironmentDirectory(val directory: String)
