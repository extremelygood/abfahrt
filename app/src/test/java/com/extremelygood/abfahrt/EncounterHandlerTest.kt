package com.extremelygood.abfahrt

import android.util.Log
import com.extremelygood.abfahrt.classes.*
import com.extremelygood.abfahrt.network.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test


class EncounterHandlerTest {

    private lateinit var connection: NearbyConnection
    private lateinit var database: DatabaseManager
    private lateinit var handler: EncounterHandler

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        MockKAnnotations.init(this)

        connection = mockk(relaxed = true)
        database = mockk(relaxed = true)

        handler = EncounterHandler(connection, database)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `start sends RequestEncountersListPacket`() = runTest {
        handler.start()

        coVerify {
            connection.sendPacket(match { it is RequestEncountersListPacket }, emptyList())
        }
    }

    @Test
    fun `onPacketReceive with EncounterPacket saves new profile`() = runTest {
        val profile = UserProfile(id = "123", firstName = "Test")
        val packet = EncounterPacket(TransmittedEncounter(profile, System.currentTimeMillis()))
        val combined = ParsedCombinedPacket(packet, mutableMapOf())

        coEvery { database.getMatchProfile("123") } returns null

        handler.start()
        handler.javaClass.getDeclaredMethod("onPacketReceive", ParsedCombinedPacket::class.java)
            .apply { isAccessible = true }
            .invoke(handler, combined)

        coVerify {
            database.saveMatchProfile(match { it.userId == "123" && it.firstName == "Test" })
        }
    }

    @Test
    fun `handleRequestEncounter sends matching profiles`() = runTest {
        val myProfile = UserProfile(id = "me", firstName = "Self")
        val matchProfile = MatchProfile(
            userId = "456", firstName = "Other", lastName = "", age = 30,
            description = "", isDriver = false, destination = GeoLocation()
        )

        coEvery { database.loadMyProfile() } returns myProfile
        coEvery { database.getAllMatches(any()) } returns listOf(matchProfile)

        val packet = RequestEncountersPacket(listOf("me", "456"))
        val combined = ParsedCombinedPacket(packet, mutableMapOf())

        handler.javaClass.getDeclaredMethod("onPacketReceive", ParsedCombinedPacket::class.java)
            .apply { isAccessible = true }
            .invoke(handler, combined)

        coVerify(exactly = 2) {
            connection.sendPacket(ofType(EncounterPacket::class), emptyList())
        }
    }

    @Test
    fun `onDisconnectCallback cancels mainJob`() {
        val job = Job()
        handler.start()
        handler.javaClass.getDeclaredField("mainJob").apply {
            isAccessible = true
            set(handler, job)
        }

        val disconnectCallbackSlot = slot<() -> Unit>()
        verify { connection.setDisconnectCallback(capture(disconnectCallbackSlot)) }

        disconnectCallbackSlot.captured.invoke()

        assert(job.isCancelled)
    }
}
