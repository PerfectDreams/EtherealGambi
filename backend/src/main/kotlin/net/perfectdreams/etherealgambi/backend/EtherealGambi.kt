package net.perfectdreams.etherealgambi.backend

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cachingheaders.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.perfectdreams.etherealgambi.backend.routes.GetFileRoute
import net.perfectdreams.etherealgambi.backend.routes.api.v1.PostUploadFileRoute
import net.perfectdreams.etherealgambi.backend.routes.api.v1.PostVariantsRoute
import net.perfectdreams.etherealgambi.backend.utils.*
import net.perfectdreams.etherealgambi.data.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class EtherealGambi(val config: EtherealGambiConfig) {
    companion object {
        const val OPTIMIZATION_VERSION = 0
        private val logger = KotlinLogging.logger {}
    }

    val files = File("./files")
    val generatedFiles = File("./generated_files")
    private val imageInfosFile = File("./image_infos.json")
    private val imageInfosTempFile = File("./image_infos.json.new")

    // https://medium.com/hceverything/applying-srcset-choosing-the-right-sizes-for-responsive-images-at-different-breakpoints-a0433450a4a3
    // https://cloudfour.com/thinks/responsive-images-the-simple-way/#what-image-sizes-should-i-provide
    val scaleDownToWidthVariantsPresets = listOf(
        ScaleDownToWidthImageVariantPreset("32w", 32),
        ScaleDownToWidthImageVariantPreset("64w", 64),
        ScaleDownToWidthImageVariantPreset("160w", 160),
        ScaleDownToWidthImageVariantPreset("320w", 320),
        ScaleDownToWidthImageVariantPreset("640w", 640),
        ScaleDownToWidthImageVariantPreset("960w", 960),
        ScaleDownToWidthImageVariantPreset("1280w", 1280),
        ScaleDownToWidthImageVariantPreset("1920w", 1920),
        ScaleDownToWidthImageVariantPreset("2560w", 2560)
    )

    private val routes = listOf(
        GetFileRoute(this),
        PostVariantsRoute(this),
        PostUploadFileRoute(this)
    )

    var originalImageInfos = ConcurrentHashMap<String, ImageInfo>()

    val pngQuant = PNGQuant(System.getenv("EG_PNGQUANT_PATH") ?: "/usr/bin/pngquant")
    private val loadAndSaveMutex = Mutex()

    private val typesToCache = listOf(
        ContentType.Image.Any,
        ContentType.Video.Any
    )

    suspend fun start() {
        files.mkdirs()
        generatedFiles.mkdirs()

        loadImageInfo()

        Runtime.getRuntime().addShutdownHook(
            thread(false) {
                runBlocking {
                    saveImageInfo()
                }
            }
        )

        val server = embeddedServer(Netty) {
            // Enables gzip and deflate compression
            install(Compression)

            // Enables caching for the specified types in the typesToCache list
            install(CachingHeaders) {
                options { call, outgoingContent ->
                    val contentType = outgoingContent.contentType
                    if (contentType != null) {
                        val contentTypeWithoutParameters = contentType.withoutParameters()
                        val matches = typesToCache.any { contentTypeWithoutParameters.match(it) || contentTypeWithoutParameters == it }

                        if (matches)
                            CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 365 * 24 * 3600))
                        else
                            null
                    } else null
                }
            }

            // Useful for images in a JavaScript canvas, since by default you CAN'T manipulate external images in a Canvas
            install(CORS) {
                anyHost()
                allowMethod(HttpMethod.Get)
                allowMethod(HttpMethod.Post)
                allowMethod(HttpMethod.Options)
                allowMethod(HttpMethod.Put)
                allowMethod(HttpMethod.Patch)
                allowMethod(HttpMethod.Delete)
                allowHeader(HttpHeaders.AccessControlAllowOrigin)
                allowHeader(HttpHeaders.ContentType)
            }

            routing {
                for (route in routes) {
                    route.register(this)
                }
            }
        }

        server.start(true)
    }

    fun createImageInfoForImage(imagePathWithoutExtension: String): ImageInfo? {
        return originalImageInfos.getOrPut(imagePathWithoutExtension) {
            val imageType = ImageType.values().firstOrNull {
                val fileToBeRead = File(
                    files,
                    imagePathWithoutExtension + "." + it.extension
                )

                fileToBeRead.exists()
            } ?: return null

            val fileToBeRead = File(files, imagePathWithoutExtension + "." + imageType.extension)
            val imageInfo = SimpleImageInfo(fileToBeRead)

            val data = ImageInfoData(
                imagePathWithoutExtension + "." + imageType.extension,
                imageType,
                imageInfo.width,
                imageInfo.height,
                fileToBeRead.length(),
                mutableListOf()
            )

            when (imageType) {
                ImageType.PNG -> PNGImageInfo(this, data)
                ImageType.GIF -> GIFImageInfo(this, data)
            }
        }
    }

    fun getVariantFromFileName(fileNameWithUnknownVariant: String): VariantResult {
        val nameWithoutExtension = fileNameWithUnknownVariant.substringBeforeLast(".")
        val extension = fileNameWithUnknownVariant.substringAfterLast(".")
            .lowercase()
        val imageType = ImageType.values().firstOrNull { it.extension == extension } ?: return UnsupportedVariantImageFormat

        val variantResult = GetFileRoute.variantRegex.findAll(nameWithoutExtension)
            .lastOrNull()

        // GIF doesn't support scaling down... yet
        if (variantResult != null && imageType != ImageType.GIF) {
            val variantPreset = scaleDownToWidthVariantsPresets.firstOrNull { it.name == variantResult.groupValues[1] } ?: return VariantNotFound

            val fileNameWithoutTheVariant = nameWithoutExtension.replace("@${variantPreset.name}", "")

            return VariantFound(
                ScaleDownToWidthImageVariant(
                    imageType,
                    variantPreset
                ),
                fileNameWithoutTheVariant
            )
        } else {
            return VariantFound(
                DefaultImageVariant(imageType),
                nameWithoutExtension
            )
        }
    }

    suspend fun loadImageInfo() {
        if (!imageInfosFile.exists()) {
            logger.warn { "\"$imageInfosFile\" does not exist!" }
            return
        }

        if (imageInfosTempFile.exists()) {
            logger.warn { "The \"$imageInfosTempFile\" file is present! This means that something went wrong while writting the image informations file during shutdown! Please check its content and remove it if it is corrupted!" }
            exitProcess(1)
        }

        loadAndSaveMutex.withLock {
            logger.info { "Loading stored image informations from \"$imageInfosFile\"..." }

            originalImageInfos =
                ConcurrentHashMap(
                    Json.decodeFromString<Map<String, ImageInfoData>>(imageInfosFile.readText())
                        .map {
                            it.key to when (it.value.imageType) {
                                ImageType.PNG -> PNGImageInfo(this, it.value)
                                ImageType.GIF -> GIFImageInfo(this, it.value)
                            }
                        }
                        .toMap()
                )
        }
    }

    suspend fun saveImageInfo() {
        loadAndSaveMutex.withLock {
            logger.info { "Saving image informations to \"$imageInfosTempFile\"..." }

            // While Atomic moves are nice, they don't seem to play nice with Docker binds :(
            // imageInfosTempFile.writeText(
            imageInfosFile.writeText(
                Json.encodeToString(
                    originalImageInfos.map {
                        it.key to it.value.data
                    }.toMap()
                )
            )

            // By using atomic moves, we can be 100% sure that the imagesInfoFile will NEVER have a "half written" file
            // imageInfosTempFile.toPath().moveTo(imageInfosFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        }
    }

    sealed class VariantResult
    object VariantNotFound : VariantResult()
    object UnsupportedVariantImageFormat : VariantResult()
    data class VariantFound(
        val variant: ImageVariant,
        val nameWithoutExtension: String,
    ) : VariantResult()
}