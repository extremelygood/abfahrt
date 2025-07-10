package com.extremelygood.abfahrt.network

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

val TEST_TRANSMITTER_NAME: String = Random.nextInt(100_000).toString()

/**
 * Class for managing a nearby connection
 */
class NearbyConnectionManager(
    private val context: Context,
    private val channelName: String
) {
    private val connectionScope = CoroutineScope(Dispatchers.Default)

    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val connectionsMap: MutableMap<CharSequence, NearbyConnection> = mutableMapOf()

    private var onConnectionEstablishedCallback: ((NearbyConnection) -> Unit)? = null

    fun setOnConnectionEstablished(callback: (NearbyConnection) -> Unit) {
        this.onConnectionEstablishedCallback = callback
    }

    fun sendPayload(endpointId: CharSequence, payload: Payload) {
        connectionsClient.sendPayload(endpointId.toString(), payload)
    }


    /**
     * Method to start advertising and automatically try to connect to them
     */
    fun startAdvertising() {
        val optionsBuilder: AdvertisingOptions.Builder = AdvertisingOptions.Builder();
        val advertisingOptions: AdvertisingOptions = optionsBuilder.build()


        val advertisement = connectionsClient.startAdvertising(TEST_TRANSMITTER_NAME, channelName, newLifecycleCallback(), advertisingOptions)
    }

    /**
     * Method to start discovery and automatically accept connection
     */
    fun startDiscovery() {
        val optionsBuilder: DiscoveryOptions.Builder = DiscoveryOptions.Builder();
        val discoveryOptions: DiscoveryOptions = optionsBuilder.build();

        connectionsClient.startDiscovery(channelName, newDiscoveryCallback(), discoveryOptions)
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
     * Method to get the scope for coroutines
     */
    fun getCoroutineScope(): CoroutineScope {
        return connectionScope
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

                        // Further check the status code
                        if (connectionResolution.status.statusCode != ConnectionsStatusCodes.STATUS_OK) {
                            destroyNearbyConnection(endpointId)
                        }

                    }
                    else -> {
                        destroyNearbyConnection(endpointId)
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
                if (!connectionsMap.contains(endpointId)) {
                    connectionsClient.requestConnection(TEST_TRANSMITTER_NAME, endpointId, newLifecycleCallback())
                }
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
        Log.d("NearbyConnectionManager", "Connection established")

        val connection = NearbyConnection(this, endpointID)
        connectionsMap[endpointID] = connection

        onConnectionEstablishedCallback?.invoke(connection)

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