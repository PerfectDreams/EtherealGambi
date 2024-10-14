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
import java.io.File

class GIFImageInfo(
    m: EtherealGambi,
    data: ImageInfoData
) : ImageInfo(m, data) {
    override fun getValidVariantPresets() = listOf(DefaultImageVariantPreset)

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

                    // TODO: Use gifsicle to optimize the GIF!

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
                is ScaleDownToWidthImageVariant -> error("GIFs does not support scale down variations yet!")
            }
        }
    }
}