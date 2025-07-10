package com.extremelygood.abfahrt.network

import com.google.android.gms.nearby.connection.Payload.File
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val module = SerializersModule {
    polymorphic(BaseDataPacket::class) {
        subclass(RequestHeartbeat::class)
        subclass(AcknowledgeHeartbeat::class)
        subclass(EncounterPacket::class)
        subclass(EncountersListPacket::class)
        subclass(ImagePacket::class)
        subclass(RequestEncountersListPacket::class)
        subclass(RequestEncountersPacket::class)
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
class RequestHeartbeat : BaseDataPacket()

@Serializable
@SerialName("HEARTBEAT_ACKNOWLEDGE")
class AcknowledgeHeartbeat : BaseDataPacket()

/**
 * Packet to transmit an encounter this device has made (may include its own profile as an encounter)
 */
@Serializable
@SerialName("ENCOUNTER")
class EncounterPacket(val encounter: TransmittedEncounter) : BaseDataPacket()

/**
 * Packet to transmit a list of Ids this client possesses
 */
@Serializable
@SerialName("ENCOUNTERS_LIST")
class EncountersListPacket(val profileIdslist: List<String>) : BaseDataPacket()

/**
 * Packet to transmit an image, with userId and imageType
 */
@Serializable
@SerialName("IMAGE")
class ImagePacket(val userId: String, val imageType: String): BaseDataPacket()


// Requests


/**
 * Packet to invoke the peer to send a list of their stored profiles. Used to discover new profiles
 * (but without any data! just their IDs)
 */
@Serializable
@SerialName("REQUEST_ENCOUNTERS_LIST")
class RequestEncountersListPacket() : BaseDataPacket()

/**
 * Packet to request a specific set of profiles, after looking through the peers offer
 */
@Serializable
@SerialName("REQUEST_ENCOUNTERS")
class RequestEncountersPacket(val profileIdsList: List<String>) : BaseDataPacket()


/**
 * Packet to request an image from peer, with userId and imageType
 */
@Serializable
@SerialName("REQUEST_IMAGE")
class RequestImagePacket(val userId: String, val imageType: String) : BaseDataPacket()
