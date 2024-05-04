val kotlinVersion: String by project
val ktorVersion: String by project
val kotlinxSerializationVersion: String by project

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.0.0-RC2"
}


group = "us.jwf"
version = "1.0-SNAPSHOT"

kotlin {
    jvm()
    linuxArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
            }
        }
    }
}