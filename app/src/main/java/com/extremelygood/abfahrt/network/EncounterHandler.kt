package com.extremelygood.abfahrt.network

import com.extremelygood.abfahrt.network.packets.ParsedCombinedPacket
import com.extremelygood.abfahrt.network.packets.RequestProfilePacket

/**
 * Class for handling a connection made to another device
 */
class EncounterHandler(
    private val connection: NearbyConnection
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

    }

    private fun onPacketReceive(combinedPacket: ParsedCombinedPacket) {
        when (combinedPacket.metaPacket) {
            is RequestProfilePacket -> {

            }
        }
    }




}