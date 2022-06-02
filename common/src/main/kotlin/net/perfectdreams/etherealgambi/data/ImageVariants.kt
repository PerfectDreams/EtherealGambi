package net.perfectdreams.etherealgambi.data

import kotlinx.serialization.Serializable

@Serializable
sealed class ImageVariant {
    abstract val imageType: ImageType
    abstract fun variantWithPrefix(): String
}

@Serializable
data class DefaultImageVariant(override val imageType: ImageType) : ImageVariant() {
    override fun variantWithPrefix() = ""
}

@Serializable
data class ScaleDownToWidthImageVariant(
    override val imageType: ImageType,
    val preset: ScaleDownToWidthImageVariantPreset
) : ImageVariant() {
    override fun variantWithPrefix() = preset.variantWithPrefix()

    fun scaleDownToWidth(originalWidth: Int, originalHeight: Int): Pair<Int, Int> {
        return Pair(
            preset.width,
            (originalHeight * preset.width) / originalWidth
        )
    }
}