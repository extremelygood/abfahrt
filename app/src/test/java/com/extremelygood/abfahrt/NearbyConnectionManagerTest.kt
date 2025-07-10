package com.extremelygood.abfahrt.networkTests

import android.content.Context
import com.extremelygood.abfahrt.network.NearbyConnectionManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import io.mockk.*
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test

class NearbyConnectionManagerTest {

    private lateinit var context: Context
    private lateinit var mockConnectionsClient: ConnectionsClient
    private lateinit var manager: NearbyConnectionManager

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mockConnectionsClient = mockk(relaxed = true)

        // Mock statische Methode Nearby.getConnectionsClient(context)
        mockkStatic(Nearby::class)
        every { Nearby.getConnectionsClient(context) } returns mockConnectionsClient

        manager = NearbyConnectionManager(context, "test-channel")
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `sendPayload sends data via connectionsClient`() {
        val payload = Payload.fromBytes("hello".toByteArray())
        manager.sendPayload("endpoint-1", payload)

        verify { mockConnectionsClient.sendPayload("endpoint-1", payload) }
    }

    @Test
    fun `disconnectFromEndpoint calls disconnect on connectionsClient`() {
        manager.disconnectFromEndpoint("endpoint-2")
        verify { mockConnectionsClient.disconnectFromEndpoint("endpoint-2") }
    }

    @Test
    fun `getCoroutineScope returns valid scope`() {
        val scope = manager.getCoroutineScope()
        assertTrue(scope.coroutineContext[Dispatchers.Default.key] != null)
    }

    @Test
    fun `startAdvertising starts advertising`() {
        manager.startAdvertising()

        verify {
            mockConnectionsClient.startAdvertising(
                any<String>(),        // Name
                "test-channel",       // Channel name (explizit)
                any<ConnectionLifecycleCallback>(),
                any<AdvertisingOptions>()
            )
        }

    }

    @Test
    fun `startDiscovery starts discovery`() {
        manager.startDiscovery()

        verify {
            mockConnectionsClient.startDiscovery(
                "test-channel",
                any(),
                any()
            )
        }
    }

    @Test
    fun `stopAdvertising stops advertising`() {
        manager.stopAdvertising()
        verify { mockConnectionsClient.stopAdvertising() }
    }
}
