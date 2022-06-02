package net.perfectdreams.etherealgambi.backend.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.etherealgambi.backend.EtherealGambi
import net.perfectdreams.etherealgambi.data.DefaultImageVariant
import net.perfectdreams.etherealgambi.data.DefaultImageVariantPreset
import net.perfectdreams.etherealgambi.data.ImageInfoData
import net.perfectdreams.etherealgambi.data.ImageType
import net.perfectdreams.etherealgambi.data.ImageVariant
import net.perfectdreams.etherealgambi.data.ImageVariantInfoData
import net.perfectdreams.etherealgambi.data.ScaleDownToWidthImageVariant
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class PNGImageInfo(
    m: EtherealGambi,
    data: ImageInfoData
) : ImageInfo(m, data) {
    override fun getValidVariantPresets() = m.scaleDownToWidthVariantsPresets.filter { width > it.width } + DefaultImageVariantPreset

    override suspend fun createImageVariant(
        variant: ImageVariant
    ): File {
        return mutexes.getOrPut(variant) { Mutex() }.withLock {
            val storedVariant = data.variants.firstOrNull { it.variantAttributes == variant }

            if (storedVariant != null) {
                val storedVariantFile = File(m.generatedFiles, storedVariant.path)
                if (storedVariant.optimizationVersion == EtherealGambi.OPTIMIZATION_VERSION && storedVariantFile.exists())
                    return@withLock storedVariantFile

                data.variants.removeIf { it.variantAttributes == variant }
            }

            val originalImageFile = File(m.files, path)
            val generatedImageFile = File(m.generatedFiles, "$folder/${fileName.name}${variant.variantWithPrefix()}.${variant.imageType.extension}")

            when (variant) {
                is DefaultImageVariant -> {
                    generatedImageFile.parentFile.mkdirs()

                    generatedImageFile.writeBytes(originalImageFile.readBytes())

                    // We don't care about the results
                    if (variant.imageType != ImageType.GIF)
                        m.pngQuant.optimize(generatedImageFile)

                    data.variants.add(
                        ImageVariantInfoData(
                            EtherealGambi.OPTIMIZATION_VERSION,
                            path,
                            variant,
                            generatedImageFile.length()
                        )
                    )

                    return@withLock generatedImageFile
                }
                is ScaleDownToWidthImageVariant -> {
                    val pathWithVariant = "$folder/${fileName.name}${variant.variantWithPrefix()}.${variant.imageType.extension}"

                    generatedImageFile.parentFile.mkdirs()

                    if (variant.preset.width > width) {
                        // If the width is bigger than what we want, get the default variant
                        return@withLock createImageVariant(DefaultImageVariant(variant.imageType))
                    } else {
                        val image = ImageIO.read(originalImageFile)
                        val (newWidth, newHeight) = variant.scaleDownToWidth(image.width, image.height)

                        val scaled = image.getScaledInstance(
                            newWidth,
                            newHeight,
                            BufferedImage.SCALE_SMOOTH
                        )

                        val x = BufferedImage(
                            scaled.getWidth(null),
                            scaled.getHeight(null),
                            BufferedImage.TYPE_INT_ARGB
                        )
                        x.createGraphics().drawImage(scaled, 0, 0, null)

                        ImageIO.write(
                            x,
                            when (variant.imageType) {
                                ImageType.PNG -> "png"
                                ImageType.GIF -> "gif"
                                // ImageType.JPEG -> "jpeg"
                            },
                            generatedImageFile
                        )

                        m.pngQuant.optimize(generatedImageFile)

                        data.variants.add(
                            ImageVariantInfoData(
                                EtherealGambi.OPTIMIZATION_VERSION,
                                pathWithVariant,
                                variant,
                                generatedImageFile.length()
                            )
                        )

                        data.variants.add(
                            ImageVariantInfoData(
                                EtherealGambi.OPTIMIZATION_VERSION,
                                pathWithVariant,
                                variant,
                                generatedImageFile.length()
                            )
                        )

                        return@withLock generatedImageFile
                    }
                }
            }
        }
    }
}