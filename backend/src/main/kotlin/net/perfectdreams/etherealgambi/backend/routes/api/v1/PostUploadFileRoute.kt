package net.perfectdreams.etherealgambi.backend.routes.api.v1

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.etherealgambi.backend.EtherealGambi
import net.perfectdreams.etherealgambi.data.DefaultImageVariantPreset
import net.perfectdreams.etherealgambi.data.ScaleDownToWidthImageVariantPreset
import net.perfectdreams.etherealgambi.data.api.UploadFileRequest
import net.perfectdreams.etherealgambi.data.api.UploadFileResponse
import net.perfectdreams.etherealgambi.data.api.requests.ImageVariantsRequest
import net.perfectdreams.etherealgambi.data.api.responses.ImageVariantsResponse
import net.perfectdreams.sequins.ktor.BaseRoute
import java.io.File
import java.util.*

class PostUploadFileRoute(val m: EtherealGambi) : BaseRoute("/api/v1/upload") {
    override suspend fun onRequest(call: ApplicationCall) {
        val authorization = call.request.header("Authorization")
        if (authorization == null) {
            call.respondText(Json.encodeToString<UploadFileResponse>(UploadFileResponse.Unauthorized))
            return
        }

        val authorizationToken = m.config.authorizationTokens.firstOrNull { it.token == authorization }
        if (authorizationToken == null) {
            call.respondText(Json.encodeToString<UploadFileResponse>(UploadFileResponse.Unauthorized))
            return
        }

        val request = Json.decodeFromString<UploadFileRequest>(call.receiveText())

        val fileData = Base64.getDecoder().decode(request.dataBase64)

        if (request.path.contains("..")) {
            call.respondText(Json.encodeToString<UploadFileResponse>(UploadFileResponse.PathTraversalDisallowed))
            return
        }

        val writeToPath = "/${authorizationToken.folder}/${request.path}"
        val file = File(m.files, writeToPath)
        if (request.failIfFileAlreadyExists && file.exists()) {
            call.respondText(Json.encodeToString<UploadFileResponse>(UploadFileResponse.FileAlreadyExists))
            return
        }

        val folder = file.parentFile
        folder.mkdirs()
        file.writeBytes(fileData)

        call.respondText(Json.encodeToString<UploadFileResponse>(UploadFileResponse.Success(writeToPath)))
    }
}