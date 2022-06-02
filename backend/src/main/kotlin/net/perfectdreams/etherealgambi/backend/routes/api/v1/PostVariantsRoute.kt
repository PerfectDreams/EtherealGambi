package net.perfectdreams.etherealgambi.backend.routes.api.v1

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.etherealgambi.backend.EtherealGambi
import net.perfectdreams.etherealgambi.data.DefaultImageVariantPreset
import net.perfectdreams.etherealgambi.data.ScaleDownToWidthImageVariantPreset
import net.perfectdreams.etherealgambi.data.api.requests.ImageVariantsRequest
import net.perfectdreams.etherealgambi.data.api.responses.ImageVariantsResponse
import net.perfectdreams.sequins.ktor.BaseRoute

class PostVariantsRoute(val m: EtherealGambi) : BaseRoute("/api/v1/variants") {
    override suspend fun onRequest(call: ApplicationCall) {
        val body = Json.decodeFromString<ImageVariantsRequest>(call.receiveText())

        val originalImageInfos = body.paths
            .mapNotNull {
                m.createImageInfoForImage(it)
            }.associate {
                // Let's not send the entire ImageInfoData because it can have a lot of variations on it
                val responseImageInfoData = ImageVariantsResponse.ImageInfoData(
                    it.data.path,
                    it.data.imageType,
                    it.data.width,
                    it.data.height,
                    it.data.size
                )

                val variantPresets = it.getValidVariantPresets().map { variantPreset ->
                    when (variantPreset) {
                        DefaultImageVariantPreset -> {
                            ImageVariantsResponse.ImageVariantResponse(variantPreset, it.pathWithoutExtension)
                        }
                        is ScaleDownToWidthImageVariantPreset -> {
                            ImageVariantsResponse.ImageVariantResponse(variantPreset, it.pathWithoutExtension + variantPreset.variantWithPrefix())
                        }
                    }
                }

                it.pathWithoutExtension to ImageVariantsResponse(
                    responseImageInfoData,
                    variantPresets
                )
            }

        call.respondText(Json.encodeToString(originalImageInfos))
    }
}