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
                implementation("io.ktor:ktor-client-core:2.3.10")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:2.0.0-RC2")
            }
        }
    }
}