package com.extremelygood.abfahrt.network

import com.extremelygood.abfahrt.classes.UserProfile
import com.google.android.gms.nearby.connection.Payload.File
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val module = SerializersModule {
    polymorphic(BaseDataPacket::class) {
        subclass(HeartbeatPacket::class)
        subclass(ProfilePacket::class)
        subclass(RequestProfilePacket::class)
        subclass(RequestImagePacket::class)
    }
}

val PacketFormat = Json { serializersModule = module }


class ParsedCombinedPacket(
    val metaPacket: BaseDataPacket,
    val files: MutableMap<Long, File>
)

@Serializable
abstract class BaseDataPacket(
    open var associatedFileIds: MutableList<Long> = mutableListOf()
)


@Serializable
@SerialName("HEARTBEAT")
class HeartbeatPacket : BaseDataPacket()

@Serializable
@SerialName("PROFILE")
class ProfilePacket(val profile: UserProfile) : BaseDataPacket()

@Serializable
@SerialName("IMAGE")
class ImagePacket(val imageType: String)

// Requests

@Serializable
@SerialName("REQUEST_PROFILE")
class RequestProfilePacket() : BaseDataPacket()

@Serializable
@SerialName("REQUEST_IMAGE")
class RequestImagePacket(val userId: String, val imageType: String) : BaseDataPacket()
