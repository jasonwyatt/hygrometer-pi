import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.0.0-RC2"
    id("com.android.library")
    id("kotlin-parcelize")
}


group = "us.jwf"
version = "1.0-SNAPSHOT"

android {
    namespace = "us.jwf.hygrometer.common"
    compileSdk = 34
}

kotlin {
    java {
        jvmToolchain(17)
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            // ...
            freeCompilerArgs.addAll("-P", "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=us.jwf.hygrometer.common.util.Parcelize")
        }
    }
    jvm()
    linuxArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.serialization)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.kotlinx.parcelize.runtime)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.parcelize.runtime)
            }
        }
    }
}