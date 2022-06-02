package net.perfectdreams.etherealgambi.data.api

import net.perfectdreams.etherealgambi.data.ImageInfoData

val ImageInfoData.fileName: FileName
    get() = FileName(path.substringAfterLast("/"))
val ImageInfoData.folder
    get() = path.substringBeforeLast("/")

data class FileName(
    val fileName: String
) {
    val name: String
        get() = fileName.substringBeforeLast(".")
    val extension: String
        get() = fileName.substringAfterLast(".")
}