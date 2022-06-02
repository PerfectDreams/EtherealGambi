package net.perfectdreams.etherealgambi.backend

import mu.KotlinLogging
import java.io.File

class PNGQuant(private val path: String) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun optimize(file: File): Result {
        val originalFileSize = file.length()

        // https://stackoverflow.com/questions/39894913/how-do-i-get-the-best-png-compression-with-gulp-imagemin-plugins
        val proc = ProcessBuilder(
            path,
            "--quality=90-100",
            "--strip",
            "-f",
            "--ext",
            ".png",
            "--skip-if-larger",
            "--speed",
            "1",
            file.absolutePath
        ).start()

        val result = proc.inputStream.readAllBytes()
        val errorStreamResult = proc.errorStream.readAllBytes()

        val s = proc.waitFor()

        // println("pngquant's input stream: ${result.toString(Charsets.UTF_8)}")
        // println("pngquant's error stream: ${errorStreamResult.toString(Charsets.UTF_8)}")
        // https://manpages.debian.org/testing/pngquant/pngquant.1.en.html
        // 99 = if quality can't match what we want, pngquant exists with exit code 99
        // 98 = If conversion results in a file larger than the original, the image won't be saved and pngquant will exit with status code 98.
        if (s == 98)
            return FileLargerThanOriginal

        if (s == 99)
            return CantMatchQualityTarget

        if (s != 0) // uuuh, this shouldn't happen if this is a PNG image...
            error("Something went wrong while trying to optimize PNG image! Status = $s; Error stream: ${errorStreamResult.toString(Charsets.UTF_8)}")

        val newFileSize = file.length()
        logger.info { "Successfully optimized ${file.name}! $originalFileSize -> $newFileSize" }

        return Success
    }

    sealed class Result
    object Success : Result()
    object CantMatchQualityTarget : Result()
    object FileLargerThanOriginal : Result()
}