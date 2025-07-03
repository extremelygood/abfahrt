package com.extremelygood.abfahrt

import com.extremelygood.abfahrt.network.ImageTransferSession
import com.extremelygood.abfahrt.network.DataPacketTransferSession
import com.extremelygood.abfahrt.network.packets.BaseDataPacket
import com.google.android.gms.nearby.connection.Payload
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * Minimal stub of BaseDataPacket for testing: we only care about
 * `associatedFileIds`, everything else can stay default / empty.
 */
private class StubDataPacket(
    override val associatedFileIds: List<Long>
) : BaseDataPacket()

class DataPacketTransferSessionTest {

    /**
     * Happy‑path: every required ImageTransferSession succeeds → the session
     * should emit a ParsedCombinedPacket via the onFinished callback.
     */
    @Test
    fun `session finishes when all required file transfers succeed`() {
        val ids = listOf(1L, 2L)
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
        val dataPacket = StubDataPacket(listOf(42L))
        val session = DataPacketTransferSession(dataPacket)

        val failed = CountDownLatch(1)
        session.setOnFailCallback { failed.countDown() }

        val payload = mockPayload(42L)
        val imageSession = ImageTransferSession(payload)
        session.offerImage(imageSession)

        imageSession.fail()

        assert(failed.await(250, TimeUnit.MILLISECONDS))
    }

    /** Utility: Mockito stub for a Payload with the requested id. */
    private fun mockPayload(id: Long): Payload {
        val p: Payload = mock()
        whenever(p.id).thenReturn(id)
        whenever(p.asFile()).thenReturn(mock())
        return p
    }
}

class ImageTransferSessionTest {

    /**
     * transferSuccess() should flip isSuccess **and** trigger the onSuccess
     * callback. The implementation currently misses the second part, so this
     * test is disabled until the bug is fixed.
     */
    @Test
    fun `transferSuccess marks success and calls callback`() {
        val payload: Payload = mock()
        whenever(payload.id).thenReturn(99L)

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
        val payload: Payload = mock()
        whenever(payload.id).thenReturn(7L)

        val session = ImageTransferSession(payload)

        val failed = CountDownLatch(1)
        session.setOnFailCallback { failed.countDown() }

        session.fail()

        assert(failed.await(250, TimeUnit.MILLISECONDS))
    }
}
