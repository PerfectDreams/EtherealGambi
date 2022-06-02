plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("maven-publish")
}

dependencies {
    api(project(":common"))
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.cio)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "PerfectDreams"
            url = uri("https://repo.perfectdreams.net/")
            credentials(PasswordCredentials::class)
        }
    }
}