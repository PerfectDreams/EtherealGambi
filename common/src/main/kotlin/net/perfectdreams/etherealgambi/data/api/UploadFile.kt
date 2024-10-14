package net.perfectdreams.etherealgambi.data.api

import kotlinx.serialization.Serializable

@Serializable
data class UploadFileRequest(
    val path: String,
    val dataBase64: String
)

@Serializable
sealed class UploadFileResponse {
    @Serializable
    data object Success : UploadFileResponse()

    @Serializable
    data object Unauthorized : UploadFileResponse()
}