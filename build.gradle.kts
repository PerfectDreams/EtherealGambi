import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin apply false
    kotlin("plugin.serialization") version libs.versions.kotlin apply false
}

group = "net.perfectdreams.etherealgambi"
version = "1.0-SNAPSHOT"

allprojects {
    group = "net.perfectdreams.etherealgambi"
    version = "1.0.0"

    repositories {
        mavenCentral()
        maven("https://repo.perfectdreams.net/")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}