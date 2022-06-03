package net.perfectdreams.etherealgambi.backend.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.etherealgambi.backend.EtherealGambi
import net.perfectdreams.sequins.ktor.BaseRoute
import java.io.File

class GetFileRoute(val m: EtherealGambi) : BaseRoute("/{file...}") {
    companion object {
        val variantRegex = Regex("@([A-z0-9]+)")
    }

    override suspend fun onRequest(call: ApplicationCall) {
        try {
            val completePath = call.parameters.getAll("file") // Should never fail but who knows right

            // Incomplete path
            if (completePath == null || 2 > completePath.size)
                return

            // Trying to go up the directory tree, go away!
            if (completePath.any { it == ".." })
                return

            val fileWithUnknownVariant = completePath.last()
            val pathWithoutFile = completePath.dropLast(1)
            val variantResult = m.getVariantFromFileName(fileWithUnknownVariant)

                when (variantResult) {
                    is EtherealGambi.UnsupportedVariantImageFormat -> {
                        // Couldn't figure out an image from the URL... so let's check if we can serve the file "raw"!
                        val file = File(m.files, completePath.joinToString("/"))
                        if (file.exists())
                            call.respondBytes(file.readBytes())
                        else
                            call.respondText("File not found", status = HttpStatusCode.NotFound)
                        return
                    }
                    is EtherealGambi.VariantNotFound -> {
                        call.respondText("Unknown Variant", status = HttpStatusCode.BadRequest)
                        return
                    }
                    is EtherealGambi.VariantFound -> {
                        val (variant, fileNameWithoutVariant) = variantResult

                        val path = (pathWithoutFile + fileNameWithoutVariant).joinToString("/")

                        withContext(Dispatchers.IO) {
                            val imageInfo = m.createImageInfoForImage(path)

                            if (imageInfo == null) {
                                call.respondText("Image not found", status = HttpStatusCode.NotFound)
                                return@withContext
                            }

                            val file = imageInfo.createImageVariant(variant)

                            call.respondBytes(
                                file.readBytes(),
                                variant.imageType.contentType
                            )
                        }
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}