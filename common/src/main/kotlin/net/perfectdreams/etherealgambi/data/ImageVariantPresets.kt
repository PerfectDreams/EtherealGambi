package net.perfectdreams.etherealgambi.data

import kotlinx.serialization.Serializable

@Serializable
sealed class ImageVariantPreset

@Serializable
object DefaultImageVariantPreset : ImageVariantPreset()

@Serializable
data class ScaleDownToWidthImageVariantPreset(
    val name: String,
    val width: Int
) : ImageVariantPreset() {
    fun variantWithPrefix() = "@$name"
}