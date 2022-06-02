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
import net.perfectdreams.etherealgambi.data.ImageVariantPreset
import net.perfectdreams.etherealgambi.data.ScaleDownToWidthImageVariant
import net.perfectdreams.etherealgambi.data.api.fileName
import net.perfectdreams.etherealgambi.data.api.folder
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

abstract class ImageInfo(
    val m: EtherealGambi,
    val data: ImageInfoData
) {
    val path by data::path
    val fileName = data.fileName
    val folder = data.folder
    val width by data::width
    val height by data::height
    val size by data::size
    val pathWithoutExtension = folder + "/" + fileName.name

    // TODO: Maybe remove unused mutexes from memory later?
    val mutexes = ConcurrentHashMap<ImageVariant, Mutex>()

    abstract fun getValidVariantPresets(): List<ImageVariantPreset>

    abstract suspend fun createImageVariant(variant: ImageVariant): File
}