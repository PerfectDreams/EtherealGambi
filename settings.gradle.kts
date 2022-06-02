rootProject.name = "EtherealGambi"

include(":common")
include(":backend")
include(":client")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val kotlin = version("kotlin", "1.6.21")
            val kotlinXSerialization = version("kotlinx-serialization", "1.3.2")
            val ktor = version("ktor", "2.0.2")

            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.6.1")
            library("kotlin-logging", "io.github.microutils", "kotlin-logging").version("2.1.21")

            library("kotlinx-serialization-core", "org.jetbrains.kotlinx", "kotlinx-serialization-core").versionRef(kotlinXSerialization)
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef(kotlinXSerialization)
            library("kotlinx-serialization-protobuf", "org.jetbrains.kotlinx", "kotlinx-serialization-protobuf").versionRef(kotlinXSerialization)
            library("kotlinx-serialization-hocon", "org.jetbrains.kotlinx", "kotlinx-serialization-hocon").versionRef(kotlinXSerialization)
            library("ktor-http", "io.ktor", "ktor-http").versionRef(ktor)
            library("ktor-client-cio", "io.ktor", "ktor-client-cio").versionRef(ktor)
            library("ktor-server-netty", "io.ktor", "ktor-server-netty").versionRef(ktor)
            library("ktor-server-compression", "io.ktor", "ktor-server-compression").versionRef(ktor)
            library("ktor-server-caching-headers", "io.ktor", "ktor-server-caching-headers").versionRef(ktor)
        }
    }
}