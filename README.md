# Kenv

Kenv is a Kotlin Multiplatform library that provides a convenient and type-safe way to access environment variables and other configuration data in your projects. It is designed to be easy to use, with a simple and intuitive API that supports JVM, JS, and Wasm platforms.

## Features

- **Type-safe environment variable access**: Use the `EnvProperty` delegate to access environment variables with type safety and default values.
- **Cross-platform compatibility**: Kenv is built with Kotlin Multiplatform, allowing you to use it in your projects targeting JVM, JS, and Wasm.
- **Easy to use**: The library is designed with a simple and intuitive API, making it easy to integrate into your projects.

## Installation

To use Kenv in your project, add the following dependency to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("io.github.mikedirven:kenv:1.0.0.0")
}
```

## Usage

### Accessing Environment Variables

You can use the `EnvProperty` delegate to access environment variables in a type-safe manner. Here's an example of how to use it:

```kotlin
import io.github.mikedirven.EnvProperty

val myVar: String by EnvProperty()
val anotherVar: Int by EnvProperty(name = "CUSTOM_VAR", default = 42)
```

In this example, `myVar` will be initialized with the value of the `MY_VAR` environment variable, and `anotherVar` will be initialized with the value of `CUSTOM_VAR`, or `42` if it's not set.

## Annotations

Kenv provides a set of annotations that can be used to customize the behavior of the `EnvProperty` delegate.

### `@EnvironmentProperty`

The `@EnvironmentProperty` annotation allows you to specify a custom name for the environment variable that a property is mapped to.

```kotlin
import io.github.mikedirven.annotations.EnvironmentProperty

@EnvironmentProperty("CUSTOM_VAR")
val myVar: String by EnvProperty()
```

### `@EnvironmentDefault`

The `@EnvironmentDefault` annotation allows you to specify a default value for a property.

```kotlin
import io.github.mikedirven.annotations.EnvironmentDefault

@EnvironmentDefault("default value")
val myVar: String by EnvProperty()
```

### `@EnvironmentDirectory`

The `@EnvironmentDirectory` annotation allows you to specify a directory to search for environment files.

```kotlin
import io.github.mikedirven.annotations.EnvironmentDirectory

@EnvironmentDirectory("/path/to/env/files")
val myVar: String by EnvProperty()
```

### `@EnvironmentListProperty`

The `@EnvironmentListProperty` annotation allows you to map a property to a list of values from an environment variable.

```kotlin
import io.github.mikedirven.annotations.EnvironmentListProperty

@EnvironmentListProperty("MY_LIST")
val myList: List<String> by EnvProperty()
```

## Contributing

Contributions are welcome! If you have any ideas, suggestions, or bug reports, please open an issue or submit a pull request.

## License

Kenv is licensed under the [MIT License](LICENSE).
