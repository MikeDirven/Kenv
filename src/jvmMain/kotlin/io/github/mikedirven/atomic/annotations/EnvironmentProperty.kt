package io.github.mikedirven.atomic.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class EnvironmentProperty(val name: String)
