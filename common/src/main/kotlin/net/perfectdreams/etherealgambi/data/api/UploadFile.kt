package net.perfectdreams.etherealgambi.data.api

import kotlinx.serialization.Serializable

@Serializable
data class UploadFileRequest(
    val path: String,
    val failIfFileAlreadyExists: Boolean,
    val dataBase64: String,
)

@Serializable
sealed class UploadFileResponse {
    @Serializable
    data class Success(val path: String) : UploadFileResponse()

    @Serializable
    data object FileAlreadyExists : UploadFileResponse()

    @Serializable
    data object PathTraversalDisallowed : UploadFileResponse()

    @Serializable
    data object Unauthorized : UploadFileResponse()
}