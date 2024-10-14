package net.perfectdreams.etherealgambi.data

import io.ktor.http.*

enum class ImageType(
    val extension: String,
    val contentType: ContentType
) {
    PNG("png", ContentType.Image.PNG),
    GIF("gif", ContentType.Image.GIF),
    JPEG("jpg", ContentType.Image.JPEG)
}