package com.extremelygood.abfahrt.network.packets

import com.extremelygood.abfahrt.classes.UserProfile
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Payload.File
import kotlinx.serialization.Polymorphic
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
    }
}

val PacketFormat = Json { serializersModule = module }


class ParsedCombinedPacket(
    metaPacket: BaseDataPacket,
    files: MutableMap<Long, File>
)

@Serializable
abstract class BaseDataPacket(
    val associatedFileIds: List<Long> = mutableListOf()
)


@Serializable
@SerialName("HEARTBEAT")
class HeartbeatPacket : BaseDataPacket()

@Serializable
@SerialName("PROFILE")
class ProfilePacket(val profile: UserProfile) : BaseDataPacket()


// Requests

@Serializable
@SerialName("REQUEST_PROFILE")
class RequestProfilePacket() : BaseDataPacket()
