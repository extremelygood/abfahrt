package com.extremelygood.abfahrt.network.packets

import com.extremelygood.abfahrt.classes.UserProfile
import kotlinx.serialization.Serializable

@Serializable
abstract class BaseDataPacket(
    val message: String
)


@Serializable
class HeartbeatPacket : BaseDataPacket("HEARTBEAT_PACKET") {

}

@Serializable
class ProfilePacket(val profile: UserProfile) : BaseDataPacket("PROFILE_PACKET")


// Requests

@Serializable
class RequestProfilePacket() : BaseDataPacket("REQUEST_PROFILE_PACKET")
