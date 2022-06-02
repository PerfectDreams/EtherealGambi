package net.perfectdreams.etherealgambi.backend

import kotlinx.coroutines.runBlocking

object EtherealGambiLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val m = EtherealGambi()

        runBlocking {
            m.start()
        }
    }
}