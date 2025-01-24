plugins {
    alias(libs.plugins.kotlinMultiPlatform)
    alias(libs.plugins.kotlinSerialization)
    id("maven-publish")
}

group = "nl.icsvertex"
version = "1.0.0.0"

repositories {
    mavenCentral()
}



kotlin {
    jvmToolchain(17)

    jvm {
        mavenPublication {
            artifactId = "kotlin-env-jvm"
        }
        withJava()
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