@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiPlatform)
    alias(libs.plugins.kotlinSerialization)
    id("maven-publish")
}

group = "io.github.MikeDirven"
version = "1.0.0.0"

repositories {
    mavenCentral()
}

publishing {
    repositories {
        mavenLocal()
        maven("https://maven.pkg.github.com/MikeDirven/Kenv") {
            name = "Github_repository"

            credentials {
                username = System.getenv("GITHUB_USER")
                password = System.getenv("GITHUB_KEY") ?: System.getenv("GITHUB_PASS")
            }
        }
    }
}

kotlin {
    jvmToolchain(21)

    jvm {
    }

    js {
        browser {
            generateTypeScriptDefinitions()
        }
    }

    wasmJs {
        browser()
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