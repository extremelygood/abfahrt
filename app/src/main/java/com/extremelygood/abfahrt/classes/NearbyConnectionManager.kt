package com.extremelygood.abfahrt.classes

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


    /**
     * Method to start advertising and automatically try to connect to them
     */
    public fun startAdvertising() {
        val optionsBuilder: AdvertisingOptions.Builder = AdvertisingOptions.Builder();
        val advertisingOptions: AdvertisingOptions = optionsBuilder.build()


        val callback = object: ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                println("Connection initiated")
            }

            override fun onConnectionResult(endpointId: String, connectionResolution: ConnectionResolution) {
                println("Connection result")
            }

            override fun onDisconnected(p0: String) {
                println("Connection disconnected")
            }

        }

        val advertisement = connectionsClient.startAdvertising(TEST_TRANSMITTER_NAME, APP_IDENTIFIER, newLifecycleCallback(), advertisingOptions)
    }

    /**
     * Method to start discovery
     */
    public fun startDiscovery() {
        val optionsBuilder: DiscoveryOptions.Builder = DiscoveryOptions.Builder();
        val discoveryOptions: DiscoveryOptions = optionsBuilder.build();

        connectionsClient.startDiscovery(TEST_TRANSMITTER_NAME, newDiscoveryCallback(), discoveryOptions)
    }


    /**
     * Method to stop trying to connect to a device
     */
    public fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }


    /**
     * Method to get a lifecycle callback
     */
    private fun newLifecycleCallback(): ConnectionLifecycleCallback {
        val callback = object: ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                println("Connection initiated")
            }

            override fun onConnectionResult(endpointId: String, connectionResolution: ConnectionResolution) {
                println("Connection result")
            }

            override fun onDisconnected(p0: String) {
                println("Connection disconnected")
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
                TODO("Not yet implemented")
            }

            override fun onEndpointLost(endpointId: String) {
                TODO("Not yet implemented")
            }

        }
        return callback
    }
}