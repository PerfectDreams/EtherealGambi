package net.perfectdreams.etherealgambi.backend.utils

import kotlinx.serialization.Serializable

@Serializable
data class EtherealGambiConfig(
    val authorizationTokens: List<AuthorizationToken>
) {
    @Serializable
    data class AuthorizationToken(
        val name: String,
        val token: String
    )
}