package net.perfectdreams.etherealgambi.backend

import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.hocon.Hocon
import kotlinx.serialization.hocon.decodeFromConfig
import net.perfectdreams.etherealgambi.backend.utils.EtherealGambiConfig
import java.io.File

object EtherealGambiLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = Hocon.decodeFromConfig<EtherealGambiConfig>(ConfigFactory.parseString(File("ethereal-gambi.conf").readText(Charsets.UTF_8)).resolve())
        val m = EtherealGambi(config)

        runBlocking {
            m.start()
        }
    }
}