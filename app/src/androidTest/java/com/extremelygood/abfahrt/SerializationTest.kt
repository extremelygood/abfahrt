package com.extremelygood.abfahrt

import com.extremelygood.abfahrt.classes.UserProfile
import com.extremelygood.abfahrt.network.packets.BaseDataPacket
import com.extremelygood.abfahrt.network.packets.PacketFormat
import com.extremelygood.abfahrt.network.packets.ProfilePacket
import org.junit.Test

class SerializationTest {

    @Test
    fun packetSerializationResultTest() {
        val primaryKey = "TheKey"
        val name = "Ralf"
        val lastName = "Schumacher"
        val age = 30
        val description = "Fast driver"

        val myProfile = UserProfile(
            primaryKey,
            name,
            lastName,
            age,
            description
        )
        val myPacket = ProfilePacket(myProfile)

        val json = PacketFormat.encodeToString(BaseDataPacket.serializer(), myPacket)
        val decodedObject = PacketFormat.decodeFromString(BaseDataPacket.serializer(), json)

        when (decodedObject) {
            is ProfilePacket -> {
                if (decodedObject.profile.firstName != name) {
                    error("Incorrect name")
                } else if (decodedObject.profile.lastName != lastName) {
                    error("Incorrect last name")
                } else if (decodedObject.profile.age != age) {
                    error("Incorrect age")
                } else if (decodedObject.profile.description != description) {
                    error("Incorrect description")
                }
            }
            else -> error("Incorrect type")
        }

    }

}