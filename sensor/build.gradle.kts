plugins {
    kotlin("multiplatform")
}

group = "us.jwf"
version = "1.0-SNAPSHOT"

dependencies {
}

kotlin {
    linuxArm64("native") {
        compilations {
            "main" {
                cinterops {
                    val libgpiod by cinterops.creating {
                        defFile("src/nativeInterop/cinterop/libgpiod.def")
                        includeDirs("src/nativeInterop/cinterop/")
                    }
                }
            }
        }
        binaries {
            executable("HygrometerSensor") {
                entryPoint = "main"
            }
            all { linkerOpts.add("-Lsrc/nativeInterop/cinterop/") }
        }
    }

    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(libs.okio)
                implementation(project(":shared"))
            }
        }
    }
}