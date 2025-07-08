package com.extremelygood.abfahrt.network

import android.util.Log
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

typealias DisconnectCallback = () -> Unit
typealias PacketReceiveCallback = (packet: ParsedCombinedPacket) -> Unit

val DEFAULT_EXPIRE_TIME: Duration = 60.seconds

/**
 * Class that represents an actual connection to a device
 */
class NearbyConnection(
    private val connectionManager: NearbyConnectionManager,
    private val endpointId: CharSequence,
    private val sessionExpireTime: Duration = DEFAULT_EXPIRE_TIME,
) {
    /**
     * For associating dataPackets to file payloads
     */
    internal val fileSessions: MutableMap<Long, ImageTransferSession> = mutableMapOf()
    internal val packetSessions: ArrayList<DataPacketTransferSession> = arrayListOf()

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
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                Log.d("NearbyConnection", "Got payload receive")
                onPayloadReceived(payload)
            }

            override fun onPayloadTransferUpdate(endpointId: String, payloadTransferUpdate: PayloadTransferUpdate) {
                onPayloadTransferUpdate(payloadTransferUpdate)
            }

        }

        return newCallbackObj
    }

    /**
     * Method to send an outbound packet to peer
     * @param packet, message you wish to send
     * @param associatedFiles, any (java File!) files you wish to append to this message
     */
    fun sendPacket(packet: BaseDataPacket, associatedFiles: List<java.io.File>) {

        val filePayloads: MutableList<Payload> = mutableListOf()

        associatedFiles.forEach { file ->
            val newFilePayload = Payload.fromFile(file)
            filePayloads.add(newFilePayload)
            packet.associatedFileIds.add(newFilePayload.id)
        }

        val packetPayload = Payload.fromBytes(PacketFormat.encodeToString(packet).toByteArray())

        Log.d("NearbyConnection", "Sending byte payload")

        connectionManager.sendPayload(endpointId, packetPayload)

        filePayloads.forEach { payload ->
            connectionManager.sendPayload(endpointId, payload)
        }
    }


    private fun onPayloadReceived(payload: Payload) {
        Log.d("NearbyConnection", "Got raw payload")

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

                val packetTransferSession = DataPacketTransferSession(dataPacket, sessionExpireTime)

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

                val newSession = ImageTransferSession(payload, sessionExpireTime)
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