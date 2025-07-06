package com.extremelygood.abfahrt.network

import androidx.core.net.toFile
import com.extremelygood.abfahrt.classes.UserProfile
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import kotlin.time.Duration.Companion.milliseconds



const val ENDPOINT_ID = "50"
const val TEST_TIMEOUT_MILLIS = 5000L

class NearbyConnectionTest {

    private lateinit var connectionManager: NearbyConnectionManager
    private lateinit var nearbyConnection: NearbyConnection
    private lateinit var payloadCallback: PayloadCallback

    @Before
    fun prepare() {
        connectionManager = mockk<NearbyConnectionManager>(relaxed = true)
        nearbyConnection = NearbyConnection(connectionManager, ENDPOINT_ID, 50.milliseconds)
        payloadCallback = nearbyConnection.getPayloadCallback()
    }

    @Test
    fun testSendPacket() {
        val myDataPacket = RequestEncountersPacket(listOf("A", "B"))

        nearbyConnection.sendPacket(myDataPacket, listOf())

        verify {
            connectionManager.sendPayload(
                withArg { endpointId ->
                    assertEquals(ENDPOINT_ID, endpointId)
                },
                withArg { payload ->
                    assertEquals(Payload.Type.BYTES, payload.type)
                    val containedPacket = PacketFormat.decodeFromString<BaseDataPacket>(String(payload.asBytes()!!, Charsets.UTF_8))
                    assertTrue(containedPacket is RequestEncountersPacket)

                    val profilePacket = containedPacket as RequestEncountersPacket
                    assertEquals(myDataPacket.profileIdsList, profilePacket.profileIdsList)
                })
        }
    }


    @Test
    fun testReceivePacket() {

        val myPacket = RequestEncountersListPacket()

        val mockCallback = mockk<(ParsedCombinedPacket) -> Unit>(relaxed = true)
        nearbyConnection.setPacketReceiveCallback(mockCallback)

        payloadCallback.onPayloadReceived(ENDPOINT_ID, transformPacketToPayload(myPacket))

        verify(exactly = 1) {
            mockCallback.invoke(withArg { combinedPacket ->
                assertTrue(combinedPacket.metaPacket is RequestEncountersListPacket)
            })
        }

    }

    @Test
    fun testReceiveDataPacketAndFiles() {
        val myPacket = RequestEncountersListPacket()
        val filePayload1 = getFakeFilePayload()
        val filePayload2 = getFakeFilePayload()
        val fakeFile1 = filePayload1.asFile()!!.asUri()!!.toFile()
        val fakeFile2 = filePayload2.asFile()!!.asUri()!!.toFile()

        myPacket.associatedFileIds = mutableListOf(filePayload1.id, filePayload2.id)


        val mockCallback = mockk<(ParsedCombinedPacket) -> Unit>(relaxed = true)
        nearbyConnection.setPacketReceiveCallback(mockCallback)


        payloadCallback.onPayloadReceived(ENDPOINT_ID, transformPacketToPayload(myPacket))
        payloadCallback.onPayloadReceived(ENDPOINT_ID, filePayload1)
        payloadCallback.onPayloadReceived(ENDPOINT_ID, filePayload2)

        val transferUpdateBuilder = PayloadTransferUpdate.Builder()
        transferUpdateBuilder.setStatus(PayloadTransferUpdate.Status.SUCCESS)

        transferUpdateBuilder.setPayloadId(filePayload1.id)
        payloadCallback.onPayloadTransferUpdate(ENDPOINT_ID, transferUpdateBuilder.build())

        transferUpdateBuilder.setPayloadId(filePayload2.id)
        payloadCallback.onPayloadTransferUpdate(ENDPOINT_ID, transferUpdateBuilder.build())


        verify(exactly = 1) {
            mockCallback.invoke(withArg { combinedPacket ->
                assertTrue(combinedPacket.metaPacket is RequestEncountersListPacket)
                assertTrue(combinedPacket.files[filePayload1.id] != null)
                assertTrue(combinedPacket.files[filePayload2.id] != null)

                val fileUri1 = combinedPacket.files[filePayload1.id]!!.asUri()!!
                val javaFile1 = fileUri1.toFile()

                val fileUri2 = combinedPacket.files[filePayload2.id]!!.asUri()!!
                val javaFile2 = fileUri2.toFile()


                assertEquals(fakeFile1.length(), javaFile1.length())
                assertEquals(fakeFile2.length(), javaFile2.length())
            })
        }
    }

    @Test
    fun testReceiveFilesAndDataPacket() {
        val myPacket = RequestEncountersListPacket()
        val filePayload1 = getFakeFilePayload()
        val filePayload2 = getFakeFilePayload()
        val fakeFile1 = filePayload1.asFile()!!.asUri()!!.toFile()
        val fakeFile2 = filePayload2.asFile()!!.asUri()!!.toFile()

        myPacket.associatedFileIds = mutableListOf(filePayload1.id, filePayload2.id)


        val mockCallback = mockk<(ParsedCombinedPacket) -> Unit>(relaxed = true)
        nearbyConnection.setPacketReceiveCallback(mockCallback)

        payloadCallback.onPayloadReceived(ENDPOINT_ID, filePayload1)
        payloadCallback.onPayloadReceived(ENDPOINT_ID, filePayload2)
        payloadCallback.onPayloadReceived(ENDPOINT_ID, transformPacketToPayload(myPacket))


        val transferUpdateBuilder = PayloadTransferUpdate.Builder()
        transferUpdateBuilder.setStatus(PayloadTransferUpdate.Status.SUCCESS)

        transferUpdateBuilder.setPayloadId(filePayload1.id)
        payloadCallback.onPayloadTransferUpdate(ENDPOINT_ID, transferUpdateBuilder.build())

        transferUpdateBuilder.setPayloadId(filePayload2.id)
        payloadCallback.onPayloadTransferUpdate(ENDPOINT_ID, transferUpdateBuilder.build())


        verify(exactly = 1) {
            mockCallback.invoke(withArg { combinedPacket ->
                assertTrue(combinedPacket.metaPacket is RequestEncountersListPacket)
                assertTrue(combinedPacket.files[filePayload1.id] != null)
                assertTrue(combinedPacket.files[filePayload2.id] != null)

                val fileUri1 = combinedPacket.files[filePayload1.id]!!.asUri()!!
                val javaFile1 = fileUri1.toFile()

                val fileUri2 = combinedPacket.files[filePayload2.id]!!.asUri()!!
                val javaFile2 = fileUri2.toFile()


                assertEquals(fakeFile1.length(), javaFile1.length())
                assertEquals(fakeFile2.length(), javaFile2.length())
            })
        }
    }

    @Test
    fun testDataPacketExpire() {
        val myPacket = RequestEncountersListPacket()
        myPacket.associatedFileIds = mutableListOf<Long>(5, 9) // Will infinitely wait for non existant files

        payloadCallback.onPayloadReceived(ENDPOINT_ID, transformPacketToPayload(myPacket))

        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < TEST_TIMEOUT_MILLIS) {
            if (nearbyConnection.packetSessions.isEmpty()) break
            Thread.sleep(50)
        }
        assertTrue(nearbyConnection.packetSessions.isEmpty())

    }

    @Test()
    fun testFileExpire() {
        val myFilePayload = getFakeFilePayload()

        payloadCallback.onPayloadReceived(ENDPOINT_ID, myFilePayload)

        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < TEST_TIMEOUT_MILLIS) {
            if (nearbyConnection.fileSessions.isEmpty()) break
            Thread.sleep(50)
        }
        assertTrue(nearbyConnection.fileSessions.isEmpty())
    }

    /**
     * Method to transform a given data packet to a payload
     */
    private fun transformPacketToPayload(dataPacket: BaseDataPacket): Payload {
        return Payload.fromBytes(PacketFormat.encodeToString(dataPacket).toByteArray())
    }

    /**
     * Method to transform a Java-File into a file payload
     */
    private fun getFakeFilePayload(): Payload {
        return Payload.fromFile(getFakeFile())
    }

    /**
     * Method to get a temporary java file
     */
    private fun getFakeFile(): File {
        val tempFile = File.createTempFile("test", ".txt")
        tempFile.writeText("Hello World")
        return tempFile
    }

}