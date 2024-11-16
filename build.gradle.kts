plugins {
    kotlin("jvm") version libs.versions.kotlin apply false
    kotlin("plugin.serialization") version libs.versions.kotlin apply false
}

allprojects {
    group = "net.perfectdreams.etherealgambi"
    version = "1.0.4"

    repositories {
        mavenCentral()
        maven("https://repo.perfectdreams.net/")
    }
}