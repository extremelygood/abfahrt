package com.extremelygood.abfahrt

import com.extremelygood.abfahrt.classes.UserProfile
import com.extremelygood.abfahrt.network.BaseDataPacket
import com.extremelygood.abfahrt.network.PacketFormat
import com.extremelygood.abfahrt.network.RequestEncountersPacket
import junit.framework.TestCase.assertEquals
import org.junit.Test

class SerializationTest {

    @Test
    fun packetSerializationResultTest() {
        val myPacket = RequestEncountersPacket(listOf("A", "B"))

        val json = PacketFormat.encodeToString(BaseDataPacket.serializer(), myPacket)
        val decodedObject = PacketFormat.decodeFromString(BaseDataPacket.serializer(), json)

        when (decodedObject) {
            is RequestEncountersPacket -> {
                assertEquals(myPacket.profileIdsList, decodedObject.profileIdsList)
            }
            else -> error("Incorrect type")
        }

    }

}