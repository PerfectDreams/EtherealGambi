import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.cloud.tools.jib") version "3.3.2"
}

repositories {
    mavenCentral()
    maven("https://repo.perfectdreams.net/")
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha14")

    implementation(libs.kotlin.logging)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.caching.headers)
    implementation(libs.kotlinx.serialization.json)

    implementation(project(":common"))

    // Sequins
    implementation("net.perfectdreams.sequins.ktor:base-route:1.0.4")

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