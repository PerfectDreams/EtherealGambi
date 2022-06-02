package net.perfectdreams.etherealgambi.data.api.responses

import kotlinx.serialization.Serializable
import net.perfectdreams.etherealgambi.data.ImageType
import net.perfectdreams.etherealgambi.data.ImageVariantPreset

@Serializable
data class ImageVariantsResponse(
    val imageInfo: ImageInfoData,
    val variants: List<ImageVariantResponse>
) {
    @Serializable
    data class ImageInfoData(
        val path: String,
        val fileType: ImageType,
        val width: Int,
        val height: Int,
        val size: Long,
    )

    @Serializable
    data class ImageVariantResponse(
        val preset: ImageVariantPreset,
        val urlWithoutExtension: String
    )
}