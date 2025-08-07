plugins {
    alias(libs.plugins.kotlinMultiPlatform)
    alias(libs.plugins.kotlinSerialization)
    id("maven-publish")
}

group = "nl.icsvertex"
version = "1.0.0.24"

val user: String = System.getenv("GITHUB_USER")
val key: String = System.getenv("GITHUB_KEY")

repositories {
    mavenCentral()
}

publishing {
    repositories {
        mavenLocal()
        maven("https://maven.pkg.github.com/ICS-Vertex/kotlin_env") {
            name = "ICSVERTEX-Github"

            credentials {
                username = user
                password = key
            }
        }
    }
}

kotlin {
    jvmToolchain(21)

    jvm {
        mavenPublication {
            artifactId = "kotlin-env-jvm"
        }
    }

    js {
        mavenPublication {
            artifactId = "kotlin-env-js"
        }
        browser {
            binaries.library()
            generateTypeScriptDefinitions()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.jetbrains.kotlin.serialization.json)
                api(libs.jetbrains.kotlin.coroutines)
                implementation(libs.jetbrains.kotlin.atomic)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.jetbrains.kotlin.reflect)
            }
        }

        val jsMain by getting {
        }
    }
}