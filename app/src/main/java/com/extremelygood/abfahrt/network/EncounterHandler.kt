package com.extremelygood.abfahrt.network

import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.classes.UserProfile

/**
 * Class for handling a connection made to another device
 */
class EncounterHandler(
    private val connection: NearbyConnection,
    private val database: DatabaseManager,
) {
    init {
        connection.setDisconnectCallback {
            end()
        }

        connection.setPacketReceiveCallback { packet ->
            onPacketReceive(packet)
        }
    }



    fun start() {

    }

    fun end() {

    }

    private fun handleRequestProfile() {
        val myProfile = UserProfile() // database.getProfile() should be here

        connection.sendPacket(ProfilePacket(myProfile), listOf())
    }

    private fun onPacketReceive(combinedPacket: ParsedCombinedPacket) {
        when (combinedPacket.metaPacket) {
            is RequestProfilePacket -> {
                handleRequestProfile()
            }
        }
    }




}