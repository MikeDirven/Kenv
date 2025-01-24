package nl.icsvertex.kotlin.env.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class EnvironmentDefault(val value: String)
