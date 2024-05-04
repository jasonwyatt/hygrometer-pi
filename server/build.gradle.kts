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

application {
    mainClass.set("us.jwf.hygrometer.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(project(":shared"))
    implementation("org.jmdns:jmdns:3.5.9")
    implementation("com.squareup.okio:okio:3.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}
