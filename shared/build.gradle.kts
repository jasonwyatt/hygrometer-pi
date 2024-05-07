plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.0.0-RC2"
}


group = "us.jwf"
version = "1.0-SNAPSHOT"

kotlin {
    java {
        jvmToolchain(17)
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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
    }
}