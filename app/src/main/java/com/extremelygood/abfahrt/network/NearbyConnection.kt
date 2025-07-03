package com.extremelygood.abfahrt.network

import android.media.Image
import android.net.Uri
import androidx.core.net.toFile
import com.extremelygood.abfahrt.network.packets.BaseDataPacket
import com.extremelygood.abfahrt.network.packets.PacketFormat
import com.extremelygood.abfahrt.network.packets.ParsedCombinedPacket
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Payload.File
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import kotlinx.serialization.decodeFromString

typealias DisconnectCallback = () -> Unit
typealias PacketReceiveCallback = (packet: ParsedCombinedPacket) -> Unit


/**
 * Class that represents an actual connection to a device
 */
class NearbyConnection(
    private val connectionManager: NearbyConnectionManager,
    private val endpointId: CharSequence
) {
    /**
     * For associating dataPackets to file payloads
     */
    private val fileSessions: MutableMap<Long, ImageTransferSession> = mutableMapOf()
    private val packetSessions: ArrayList<DataPacketTransferSession> = arrayListOf()

    private var onDisconnectCallback: DisconnectCallback? = null
    private var onPacketReceiveCallback: PacketReceiveCallback? = null

    /**
     * Method to disconnect (effectively end) this connection
     * Internally calls the connection manager to disconnect
     */
    fun disconnect() {
        connectionManager.disconnectFromEndpoint(endpointId)
    }

    fun setDisconnectCallback(callback: DisconnectCallback?) {
        onDisconnectCallback = callback
    }

    fun setPacketReceiveCallback(callback: PacketReceiveCallback) {
        this.onPacketReceiveCallback = callback
    }


    /**
     * Method to be explicitly used by connection manager to communicate that connection no longer exists
     */
    fun handleDisconnection() {
        onDisconnectCallback?.invoke()
    }

    /**
     * Method used by ConnectionManager to get a payload callback
     */
    fun getPayloadCallback(): PayloadCallback {
        val newCallbackObj = object: PayloadCallback() {
            override fun onPayloadReceived(p0: String, p1: Payload) {
                onPayloadReceived(p1)
            }

            override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
                onPayloadTransferUpdate(p1)
            }

        }

        return newCallbackObj
    }


    fun sendPacket(packet: BaseDataPacket, associatedFileUris: List<Uri>) {

        val filePayloads: MutableList<Payload> = mutableListOf()

        associatedFileUris.forEach { uri ->
            val newFilePayload = Payload.fromFile(uri.toFile())
            filePayloads.add(newFilePayload)
            packet.associatedFileIds.add(newFilePayload.id)
        }

        val packetPayload = Payload.fromBytes(PacketFormat.encodeToString(packet).toByteArray())


        connectionManager.sendPayload(endpointId, packetPayload)

        filePayloads.forEach { payload ->
            connectionManager.sendPayload(endpointId, payload)
        }
    }


    private fun onPayloadReceived(payload: Payload) {

        // Small service method to assign an imageTransferSession to a living DataPacketSession
        fun offerToHost(imageSession: ImageTransferSession) {
            packetSessions.forEach { packetSession ->
                packetSession.offerImage(imageSession)
            }
        }

        fun offerAllToPacketSession(packetSession: DataPacketTransferSession) {
            fileSessions.forEach { (_, fileSession) ->
                packetSession.offerImage(fileSession)
            }
        }

        // Bytes are always received as a string, which is converted to a dataPacket
        // From this, start a new packet transfer session (there may be more packets associated with this!)
        when (payload.type) {
            Payload.Type.BYTES -> {

                val json = String(payload.asBytes()!!, Charsets.UTF_8)
                val dataPacket = PacketFormat.decodeFromString<BaseDataPacket>(json)

                val packetTransferSession = DataPacketTransferSession(dataPacket)

                // Internal method to cleanup (handled similarly in fail and success case)
                fun clearFields() {
                    packetSessions.remove(packetTransferSession)

                    dataPacket.associatedFileIds.forEach { id ->
                        fileSessions.remove(id)
                    }
                }

                packetTransferSession.setOnFinishedCallback { finishedPacket ->
                    onPacketReceiveCallback?.invoke(finishedPacket)

                    clearFields()
                }
                packetTransferSession.setOnFailCallback {
                    clearFields()
                }

                packetSessions.add(packetTransferSession)
                offerAllToPacketSession(packetTransferSession)
            }
            Payload.Type.FILE -> {

                val newSession = ImageTransferSession(payload)
                fileSessions[payload.id] = newSession

                // After offering to DataPacketSession, this will be overwritten
                // In case DataPacketSession is never found, do this manually
                newSession.setOnFailCallback {
                    fileSessions.remove(payload.id)
                }

                offerToHost(newSession)
            }
            else -> {
                error("Unknown type received through payload")
            }
        }
    }

    private fun onPayloadTransferUpdate(payloadTransferUpdate: PayloadTransferUpdate) {
        val imageSession = fileSessions[payloadTransferUpdate.payloadId] ?: return

        if (payloadTransferUpdate.status == PayloadTransferUpdate.Status.SUCCESS) {

            imageSession.transferSuccess()

        } else if (payloadTransferUpdate.status == PayloadTransferUpdate.Status.FAILURE || payloadTransferUpdate.status == PayloadTransferUpdate.Status.CANCELED) {

            imageSession.fail()

        }
    }
}