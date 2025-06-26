package com.extremelygood.abfahrt.classes


typealias DisconnectCallback = () -> Unit

/**
 * Class that represents an actual connection to a device
 */
class NearbyConnection(
    private val connectionManager: NearbyConnectionManager,
    private val endpointId: String
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
     * Primary method for sending bytes on the lower level
     */
    private fun sendBytes() {

    }

    /**
     * Primary method for receiving bytes from the peer
     */
    private fun receiveBytes() {

    }
}