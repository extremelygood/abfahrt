package com.extremelygood.abfahrt.network

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload

const val APP_IDENTIFIER: String = "com.abfahrt"
const val TEST_TRANSMITTER_NAME: String = "Richtiger Kevin"

/**
 * Class for managing a nearby connection
 */
class NearbyConnectionManager(
    private val context: Context,
    private val channelName: String
) {
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val connectionsMap: MutableMap<CharSequence, NearbyConnection> = mutableMapOf()

    fun sendPayload(endpointId: CharSequence, payload: Payload) {
        connectionsClient.sendPayload(endpointId.toString(), payload)
    }


    /**
     * Method to start advertising and automatically try to connect to them
     */
    fun startAdvertising() {
        val optionsBuilder: AdvertisingOptions.Builder = AdvertisingOptions.Builder();
        val advertisingOptions: AdvertisingOptions = optionsBuilder.build()


        val advertisement = connectionsClient.startAdvertising(TEST_TRANSMITTER_NAME, APP_IDENTIFIER, newLifecycleCallback(), advertisingOptions)
    }

    /**
     * Method to start discovery and automatically accept connection
     */
    fun startDiscovery() {
        val optionsBuilder: DiscoveryOptions.Builder = DiscoveryOptions.Builder();
        val discoveryOptions: DiscoveryOptions = optionsBuilder.build();

        connectionsClient.startDiscovery(TEST_TRANSMITTER_NAME, newDiscoveryCallback(), discoveryOptions)
    }


    /**
     * Method to stop trying to connect to a device
     */
    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    /**
     * Method to invoke the connectionsClient to disconnect from an endpoint
     */
    fun disconnectFromEndpoint(endpointId: CharSequence) {
        connectionsClient.disconnectFromEndpoint(endpointId.toString())
    }

    /**
     * Method to get a connection lifecycle callback
     */
    private fun newLifecycleCallback(): ConnectionLifecycleCallback {
        val callback = object: ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // The way we do this is that we immediately accept a request.
                // We also prematurely create the connection object, so that it can immediately start
                // handling payloads.
                // If it turns out that this connection prematurely dies via onConnectionResult, simply
                // handle disconnect gracefully
                val newConnection = connectionEstablished(endpointId)
                connectionsClient.acceptConnection(endpointId, newConnection.getPayloadCallback())
            }

            override fun onConnectionResult(endpointId: String, connectionResolution: ConnectionResolution) {
                when {
                    connectionResolution.status.isSuccess -> {
                        // Do nothing, connection already exists
                    }
                    connectionResolution.status.isCanceled -> {
                        // Destroy the connection
                        destroyNearbyConnection(endpointId)
                    }
                    connectionResolution.status.isInterrupted -> {
                        // I don't know what this case does. Research this
                        destroyNearbyConnection(endpointId)
                        TODO("Check what this does")
                    }
                }
            }

            override fun onDisconnected(endpointId: String) {
                destroyNearbyConnection(endpointId)
            }

        }
        return callback
    }

    /**
     * Method to get a discovery callback
     */
    private fun newDiscoveryCallback(): EndpointDiscoveryCallback {
        val callback = object: EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                val connectionObj = connectionEstablished(endpointId)
                connectionsClient.requestConnection(TEST_TRANSMITTER_NAME, endpointId, newLifecycleCallback())
            }

            override fun onEndpointLost(endpointId: String) {
                destroyNearbyConnection(endpointId)
            }

        }
        return callback
    }

    /**
     * Low-Level method executed when a connection is established
     */
    private fun connectionEstablished(endpointID: CharSequence): NearbyConnection {
        val connection = NearbyConnection(this, endpointID)
        connectionsMap[endpointID] = connection


        return connection
    }

    /**
     * Low-Level method executed when a connection is (to-be) destroyed
     */
    private fun destroyNearbyConnection(endpointId: CharSequence) {
        val connectionObj = connectionsMap.remove(endpointId)
        connectionObj?.handleDisconnection()
    }
}