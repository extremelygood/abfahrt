package com.extremelygood.abfahrt.network.packets

import kotlinx.serialization.Serializable

@Serializable
data class DataPacket(
    val message: String = "DEFAULT_MESSAGE"
)
