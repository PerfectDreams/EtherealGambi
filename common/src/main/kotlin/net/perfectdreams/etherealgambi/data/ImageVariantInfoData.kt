package net.perfectdreams.etherealgambi.data

import kotlinx.serialization.Serializable

@Serializable
data class ImageVariantInfoData(
    val optimizationVersion: Int,
    val path: String,
    val variantAttributes: ImageVariant,
    val size: Long
)