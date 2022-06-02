package net.perfectdreams.etherealgambi.data

import kotlinx.serialization.Serializable

@Serializable
data class ImageInfoData(
    val path: String,
    val imageType: ImageType,
    val width: Int,
    val height: Int,
    val size: Long,
    val variants: MutableList<ImageVariantInfoData>
)