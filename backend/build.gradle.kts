import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version "3.4.3"
}

repositories {
    mavenCentral()
    maven("https://repo.perfectdreams.net/")
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.10")

    implementation(libs.kotlin.logging)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.caching.headers)
    implementation(libs.ktor.server.cors)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.hocon)

    implementation(project(":common"))

    testImplementation(kotlin("test"))
}

jib {
    to {
        image = "ghcr.io/perfectdreams/etherealgambi"

        auth {
            username = System.getProperty("DOCKER_USERNAME") ?: System.getenv("DOCKER_USERNAME")
            password = System.getProperty("DOCKER_PASSWORD") ?: System.getenv("DOCKER_PASSWORD")
        }
    }

    from {
        // This image comes from the "docker" folder Dockerfile!
        // Don't forget to build the image before compiling DreamStorageService!
        // https://github.com/GoogleContainerTools/jib/issues/1468
        image = "tar://${File(rootDir, "docker/image.tar").absoluteFile}"
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}