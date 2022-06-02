package net.perfectdreams.etherealgambi.data.api.requests

import kotlinx.serialization.Serializable

@Serializable
data class ImageVariantsRequest(
    val paths: List<String>
)