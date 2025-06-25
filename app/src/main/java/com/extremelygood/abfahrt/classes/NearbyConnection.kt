package com.extremelygood.abfahrt.classes


/**
 * Class that represents an actual connection to a device
 */
class NearbyConnection(
    private val connectionManager: NearbyConnectionManager,
    private val endpointId: String
) {

    fun disconnect() {

    }
}