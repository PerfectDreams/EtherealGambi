plugins {
    kotlin("jvm") version libs.versions.kotlin apply false
    kotlin("plugin.serialization") version libs.versions.kotlin apply false
}

group = "net.perfectdreams.etherealgambi"
version = "1.0.1-SNAPSHOT"

allprojects {
    group = "net.perfectdreams.etherealgambi"
    version = "1.0.1"

    repositories {
        mavenCentral()
        maven("https://repo.perfectdreams.net/")
    }
}