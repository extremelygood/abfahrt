package com.extremelygood.abfahrt

import com.extremelygood.abfahrt.network.ImageTransferSession
import com.extremelygood.abfahrt.network.DataPacketTransferSession
import com.extremelygood.abfahrt.network.packets.BaseDataPacket
import com.google.android.gms.nearby.connection.Payload
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Minimal stub of BaseDataPacket for testing: we only care about
 * `associatedFileIds`, everything else can stay default / empty.
 */
private class StubDataPacket(
    override val associatedFileIds: MutableList<Long>
) : BaseDataPacket()

class DataPacketTransferSessionTest {

    /**
     * Happy-path: every required ImageTransferSession succeeds → the session
     * should emit a ParsedCombinedPacket via the onFinished callback.
     */
    @Test
    fun `session finishes when all required file transfers succeed`() {
        val ids = mutableListOf(1L, 2L)
        val dataPacket = StubDataPacket(ids)
        val session = DataPacketTransferSession(dataPacket)

        val finished = CountDownLatch(1)
        session.setOnFinishedCallback { combined ->
            assert(ids.toSet() == combined.files.keys)
            finished.countDown()
        }

        // Provide an ImageTransferSession for each required id and mark it successful.
        ids.forEach { id ->
            val payload = mockPayload(id)
            val imageSession = ImageTransferSession(payload)
            session.offerImage(imageSession)
            imageSession.transferSuccess()
        }

        assert(finished.await(250, TimeUnit.MILLISECONDS))
    }

    /**
     * If even one ImageTransferSession fails, the entire DataPacketTransferSession
     * must invoke its onFail callback.
     */
    @Test
    fun `session fails when any image session fails`() {
        val dataPacket = StubDataPacket(mutableListOf(42L))
        val session = DataPacketTransferSession(dataPacket)

        val failed = CountDownLatch(1)
        session.setOnFailCallback { failed.countDown() }

        val payload = mockPayload(42L)
        val imageSession = ImageTransferSession(payload)
        session.offerImage(imageSession)

        imageSession.fail()

        assert(failed.await(250, TimeUnit.MILLISECONDS))
    }

    /** Utility: mockk stub for a Payload with the requested id. */
    private fun mockPayload(id: Long): Payload {
        val p = mockk<Payload>(relaxed = true)
        every { p.id } returns id
        every { p.asFile() } returns mockk()   // relaxed stub for the file wrapper
        return p
    }
}

class ImageTransferSessionTest {

    /**
     * transferSuccess() should flip isSuccess **and** trigger the onSuccess
     * callback.
     */
    @Test
    fun `transferSuccess marks success and calls callback`() {
        val payload = mockk<Payload>()
        every { payload.id } returns 99L

        val session = ImageTransferSession(payload)

        var successFlag = false
        session.setOnSuccessCallback { successFlag = true }

        session.transferSuccess()

        assert(session.isSuccess)
        assert(successFlag)
    }

    /**
     * Verifies that `fail()` cancels expiry and triggers onFail.
     */
    @Test
    fun `fail triggers callback`() {
        val payload = mockk<Payload>()
        every { payload.id } returns 7L

        val session = ImageTransferSession(payload)

        val failed = CountDownLatch(1)
        session.setOnFailCallback { failed.countDown() }

        session.fail()

        assert(failed.await(250, TimeUnit.MILLISECONDS))
    }
}
