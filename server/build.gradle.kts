import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val kotlinxSerializationVersion: String by project

plugins {
    kotlin("jvm")
    application
}

group = "us.jwf.hygrometer"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("us.jwf.hygrometer.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.okio)
    implementation(libs.kotlinx.atomicfu)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback)
}
