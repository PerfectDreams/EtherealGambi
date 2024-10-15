package net.perfectdreams.etherealgambi.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.etherealgambi.data.api.UploadFileRequest
import net.perfectdreams.etherealgambi.data.api.UploadFileResponse
import net.perfectdreams.etherealgambi.data.api.requests.ImageVariantsRequest
import net.perfectdreams.etherealgambi.data.api.responses.ImageVariantsResponse
import java.io.Closeable
import java.util.*

class EtherealGambiClient(baseUrl: String) : Closeable {
    private val http = HttpClient(CIO)
    val baseUrl = baseUrl.removeSuffix("/") // Remove trailing slash

    suspend fun getImageInfo(vararg paths: String): Map<String, ImageVariantsResponse> {
        return Json.decodeFromString(
            http.post("$baseUrl/api/v1/variants") {
                setBody(
                    TextContent(
                        Json.encodeToString(ImageVariantsRequest(paths.toList())),
                        ContentType.Application.Json
                    )
                )
            }.bodyAsText()
        )
    }

    suspend fun uploadFile(token: String, path: String, failIfFileAlreadyExists: Boolean, data: ByteArray): UploadFileResponse {
        return Json.decodeFromString<UploadFileResponse>(
            http.post("$baseUrl/api/v1/upload") {
                header(HttpHeaders.Authorization, token)

                setBody(
                    TextContent(
                        Json.encodeToString(UploadFileRequest(path, failIfFileAlreadyExists, Base64.getEncoder().encodeToString(data))),
                        ContentType.Application.Json
                    )
                )
            }.bodyAsText()
        )
    }

    override fun close() {
        http.close()
    }
}