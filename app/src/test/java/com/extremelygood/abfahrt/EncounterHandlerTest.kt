package com.extremelygood.abfahrt

import android.util.Log
import com.extremelygood.abfahrt.classes.*
import com.extremelygood.abfahrt.network.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class EncounterHandlerTest {

    private lateinit var connection: NearbyConnection
    private lateinit var database: DatabaseManager
    private lateinit var handler: EncounterHandler

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0 // Stub Log.d

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

        handler.start() // ensure coroutineScope is alive
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
    fun `handleRequestEncountersList sends all local ids`() = runTest {
        val myProfile = UserProfile(id = "me", firstName = "Self")
        val match = MatchProfile(
            userId = "x", firstName = "X", lastName = "", age = 0,
            description = "", isDriver = false, destination = GeoLocation()
        )

        coEvery { database.loadMyProfile() } returns myProfile
        coEvery { database.getAllMatches(any()) } returns listOf(match)

        val combined = ParsedCombinedPacket(RequestEncountersListPacket(), mutableMapOf())

        handler.javaClass.getDeclaredMethod("onPacketReceive", ParsedCombinedPacket::class.java).apply {
            isAccessible = true
        }.invoke(handler, combined)

        // Warten bis alle coroutines fertig sind
        advanceUntilIdle()

        val slotPacket = slot<BaseDataPacket>()
        val slotList = slot<List<File>>()

        verify(timeout = 1000) {
            connection.sendPacket(capture(slotPacket), capture(slotList))
        }

        val sentPacket = slotPacket.captured
        assert(sentPacket is EncountersListPacket)
        val ids = (sentPacket as EncountersListPacket).profileIdslist
        assert(ids.containsAll(listOf("me", "x")))
    }


    @Test
    fun `onDisconnectCallback cancels mainJob`() {
        val job = Job()
        handler.start()
        handler.javaClass.getDeclaredField("mainJob").apply {
            isAccessible = true
            set(handler, job)
        }

        // Simuliere Disconnect
        val disconnectCallbackSlot = slot<() -> Unit>()
        verify { connection.setDisconnectCallback(capture(disconnectCallbackSlot)) }

        disconnectCallbackSlot.captured.invoke()

        assert(job.isCancelled)
    }
}
