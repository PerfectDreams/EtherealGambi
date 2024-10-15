package net.perfectdreams.etherealgambi.backend.utils

import kotlinx.serialization.Serializable

@Serializable
data class EtherealGambiConfig(
    val authorizationTokens: List<AuthorizationToken>
) {
    @Serializable
    data class AuthorizationToken(
        val name: String,
        val folder: String,
        val token: String,
    )
}