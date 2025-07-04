package com.extremelygood.abfahrt

import com.extremelygood.abfahrt.classes.UserProfile
import com.extremelygood.abfahrt.network.NearbyConnection
import com.extremelygood.abfahrt.network.NearbyConnectionManager
import com.extremelygood.abfahrt.network.packets.BaseDataPacket
import com.extremelygood.abfahrt.network.packets.HeartbeatPacket
import com.extremelygood.abfahrt.network.packets.PacketFormat
import com.extremelygood.abfahrt.network.packets.ParsedCombinedPacket
import com.extremelygood.abfahrt.network.packets.ProfilePacket
import com.extremelygood.abfahrt.network.packets.RequestProfilePacket
import com.google.android.gms.nearby.connection.Payload
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import kotlin.test.assertEquals
import kotlin.test.assertIs

const val ENDPOINT_ID = "50"

class NearbyConnectionTest {

    private lateinit var connectionManager: NearbyConnectionManager
    private lateinit var nearbyConnection: NearbyConnection

    @BeforeEach
    fun prepare() {
        connectionManager = mockk<NearbyConnectionManager>(relaxed = true)
        nearbyConnection = NearbyConnection(connectionManager, ENDPOINT_ID)
    }

    @Test
    fun testSendPacket() {
        val myDataPacket = RequestProfilePacket()

        nearbyConnection.sendPacket(myDataPacket, listOf())

        verify {
            connectionManager.sendPayload(
                withArg { endpointId ->
                    assertEquals(ENDPOINT_ID, endpointId)
                },
                withArg { payload ->
                    assertEquals(Payload.Type.BYTES, payload.type)

                    val containedPacket = PacketFormat.decodeFromString<BaseDataPacket>(String(payload.asBytes()!!, Charsets.UTF_8))
                    assertInstanceOf<RequestProfilePacket>(containedPacket)
                })
        }
    }


    @Test
    fun testReceivePacket() {

        val myPacket = RequestProfilePacket()

        val mockCallback = mockk<(ParsedCombinedPacket) -> Unit>(relaxed = true)
        nearbyConnection.setPacketReceiveCallback(mockCallback)

        val payloadCallback = nearbyConnection.getPayloadCallback()
        payloadCallback.onPayloadReceived(ENDPOINT_ID, transformPacketToPayload(myPacket))

        verify {
            mockCallback.invoke(withArg { combinedPacket ->
              assertInstanceOf<RequestProfilePacket>(combinedPacket.metaPacket)
            })
        }

    }

    /**
     * Method to transform a given data packet to a payload
     */
    private fun transformPacketToPayload(dataPacket: BaseDataPacket): Payload {
        return Payload.fromBytes(PacketFormat.encodeToString(dataPacket).toByteArray())
    }

}