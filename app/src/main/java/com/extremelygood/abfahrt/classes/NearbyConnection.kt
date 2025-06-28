package com.extremelygood.abfahrt.classes

import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate


typealias DisconnectCallback = () -> Unit

/**
 * Class that represents an actual connection to a device
 */
class NearbyConnection(
    private val connectionManager: NearbyConnectionManager,
    private val endpointId: CharSequence
) {
    private var onDisconnectCallback: DisconnectCallback? = null


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

    private fun onPayloadReceived(payload: Payload) {

    }

    private fun onPayloadTransferUpdate(payloadTransferUpdate: PayloadTransferUpdate) {

    }
}